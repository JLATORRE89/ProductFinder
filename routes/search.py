from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from database import get_db, User, Search, Product
from schemas import SearchCreate, SearchResponse, SearchHistoryResponse
from auth_utils import get_current_user
from scraper import scrape_walmart_products

router = APIRouter(prefix="/api/search", tags=["search"])

@router.post("/", response_model=SearchResponse, status_code=status.HTTP_201_CREATED)
async def create_search(
    search_data: SearchCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Perform a product search and save results
    """
    # Create search record
    search = Search(
        user_id=current_user.id,
        query=search_data.query
    )
    db.add(search)
    db.commit()
    db.refresh(search)

    # Scrape products from Walmart
    products_data = scrape_walmart_products(search_data.query, max_results=20)

    # Save products to database
    for product_data in products_data:
        product = Product(
            search_id=search.id,
            title=product_data['title'],
            url=product_data['url'],
            price=product_data.get('price'),
            status=product_data.get('status')
        )
        db.add(product)

    db.commit()
    db.refresh(search)

    return search

@router.get("/history", response_model=List[SearchHistoryResponse])
async def get_search_history(
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
    limit: int = 20
):
    """
    Get user's search history
    """
    searches = db.query(Search).filter(
        Search.user_id == current_user.id
    ).order_by(Search.created_at.desc()).limit(limit).all()

    # Format response with product count
    history = []
    for search in searches:
        history.append({
            "id": search.id,
            "query": search.query,
            "created_at": search.created_at,
            "product_count": len(search.products)
        })

    return history

@router.get("/{search_id}", response_model=SearchResponse)
async def get_search(
    search_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Get a specific search with its products
    """
    search = db.query(Search).filter(
        Search.id == search_id,
        Search.user_id == current_user.id
    ).first()

    if not search:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Search not found"
        )

    return search

@router.delete("/{search_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_search(
    search_id: int,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """
    Delete a search and its products
    """
    search = db.query(Search).filter(
        Search.id == search_id,
        Search.user_id == current_user.id
    ).first()

    if not search:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Search not found"
        )

    db.delete(search)
    db.commit()

    return None

from pydantic import BaseModel, EmailStr
from datetime import datetime
from typing import List, Optional

# User Schemas
class UserCreate(BaseModel):
    email: EmailStr
    username: str
    password: str

class UserLogin(BaseModel):
    username: str
    password: str

class UserResponse(BaseModel):
    id: int
    email: str
    username: str
    created_at: datetime

    class Config:
        from_attributes = True

class Token(BaseModel):
    access_token: str
    token_type: str

# Product Schemas
class ProductResponse(BaseModel):
    id: int
    title: str
    url: str
    status: Optional[str] = None
    price: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True

# Search Schemas
class SearchCreate(BaseModel):
    query: str

class SearchResponse(BaseModel):
    id: int
    query: str
    created_at: datetime
    products: List[ProductResponse] = []

    class Config:
        from_attributes = True

class SearchHistoryResponse(BaseModel):
    id: int
    query: str
    created_at: datetime
    product_count: int

    class Config:
        from_attributes = True

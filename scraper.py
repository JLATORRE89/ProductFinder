import requests
from bs4 import BeautifulSoup
from typing import List, Dict, Optional
import time

def scrape_walmart_products(query: str, max_results: int = 20) -> List[Dict[str, str]]:
    """
    Scrape Walmart for product information based on search query

    Args:
        query: Search term for products
        max_results: Maximum number of products to return

    Returns:
        List of dictionaries containing product information
    """
    products = []

    try:
        # Format the search URL
        search_url = f"https://www.walmart.com/search?q={query.replace(' ', '+')}"

        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
        }

        # Make request
        response = requests.get(search_url, headers=headers, timeout=10)
        response.raise_for_status()

        soup = BeautifulSoup(response.content, 'html.parser')

        # Find product containers (this selector may need adjustment based on Walmart's current HTML structure)
        # Note: Walmart uses dynamic loading, so scraping may be limited
        product_elements = soup.find_all('div', {'data-item-id': True})

        for element in product_elements[:max_results]:
            try:
                # Extract product information
                title_elem = element.find('span', class_='w_iUH7')
                link_elem = element.find('a')
                price_elem = element.find('div', class_='f_8mz w_jlBh')

                if title_elem and link_elem:
                    product = {
                        'title': title_elem.text.strip(),
                        'url': f"https://www.walmart.com{link_elem.get('href', '')}" if link_elem.get('href', '').startswith('/') else link_elem.get('href', ''),
                        'price': price_elem.text.strip() if price_elem else 'N/A',
                        'status': 'Available'  # Default status
                    }
                    products.append(product)
            except Exception as e:
                # Skip products that fail to parse
                continue

        # If no products found with the above selectors, try alternative approach
        if not products:
            # Fallback to finding links that contain product information
            all_links = soup.find_all('a', href=True)
            for link in all_links[:max_results]:
                href = link.get('href', '')
                if '/ip/' in href:  # Walmart product URLs contain /ip/
                    title = link.get('aria-label') or link.text.strip()
                    if title and len(title) > 5:
                        product = {
                            'title': title[:200],  # Limit title length
                            'url': f"https://www.walmart.com{href}" if href.startswith('/') else href,
                            'price': 'N/A',
                            'status': 'Available'
                        }
                        # Avoid duplicates
                        if product not in products:
                            products.append(product)
                            if len(products) >= max_results:
                                break

    except requests.RequestException as e:
        print(f"Error fetching Walmart data: {e}")
    except Exception as e:
        print(f"Error parsing Walmart data: {e}")

    return products

def get_product_status(product_url: str) -> Optional[str]:
    """
    Get detailed status information for a specific product

    Args:
        product_url: Full URL to the product page

    Returns:
        Status string or None
    """
    try:
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }

        response = requests.get(product_url, headers=headers, timeout=10)
        response.raise_for_status()

        soup = BeautifulSoup(response.content, 'html.parser')

        # Look for fulfillment/availability information
        fulfillment = soup.find('div', class_='fulfillment-shipping')
        if fulfillment:
            return fulfillment.text.strip()

        # Check for out of stock indicators
        out_of_stock = soup.find(text=lambda t: t and 'out of stock' in t.lower())
        if out_of_stock:
            return 'Out of Stock'

        return 'Available'

    except Exception as e:
        return None

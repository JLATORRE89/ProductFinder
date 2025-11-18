# ProductFinder MVP

A web application for searching Walmart products with user registration and search history tracking.

## Features

- User Registration and Authentication
- Product Search (Walmart)
- Search History Tracking
- JWT-based Security
- Clean, Responsive UI

## Tech Stack

**Backend:**
- FastAPI
- SQLAlchemy
- JWT Authentication
- BeautifulSoup4 (Web Scraping)
- SQLite Database

**Frontend:**
- HTML5
- CSS3
- Vanilla JavaScript
- Responsive Design

## Installation

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. The `.env` file is already configured with default values for development.

3. Run the application:
```bash
python main.py
```

The application will start at `http://localhost:8000`

## Usage

1. **Home Page** (`/`): Welcome page with links to register and login
2. **Register** (`/register`): Create a new account
3. **Login** (`/login`): Login to your account
4. **Search** (`/search`): Search for products and view history (requires login)

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and receive JWT token
- `GET /api/auth/me` - Get current user info (requires auth)

### Search
- `POST /api/search/` - Create a new search (requires auth)
- `GET /api/search/history` - Get search history (requires auth)
- `GET /api/search/{search_id}` - Get specific search details (requires auth)
- `DELETE /api/search/{search_id}` - Delete a search (requires auth)

## Database Schema

**Users Table:**
- id (Primary Key)
- email (Unique)
- username (Unique)
- hashed_password
- created_at

**Searches Table:**
- id (Primary Key)
- user_id (Foreign Key)
- query
- created_at

**Products Table:**
- id (Primary Key)
- search_id (Foreign Key)
- title
- url
- status
- price
- created_at

## Security

- Passwords are hashed using bcrypt
- JWT tokens for authentication
- CORS configured for security
- SQL injection protection via SQLAlchemy ORM

## Development

To run in development mode with auto-reload:
```bash
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

## Notes

- The web scraper may have limitations due to Walmart's dynamic content loading
- For production use, consider implementing rate limiting and caching
- Update the `SECRET_KEY` in `.env` for production deployment

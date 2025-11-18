from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse
import os
from database import init_db
from routes import auth, search
from config import get_settings

settings = get_settings()

# Initialize database
init_db()

# Create FastAPI app
app = FastAPI(
    title=settings.APP_NAME,
    debug=settings.DEBUG
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify actual origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(auth.router)
app.include_router(search.router)

# Serve static files
static_dir = os.path.join(os.path.dirname(__file__), "static")
if os.path.exists(static_dir):
    app.mount("/static", StaticFiles(directory=static_dir), name="static")

# Serve frontend pages
@app.get("/")
async def read_index():
    """Serve the main page"""
    return FileResponse("frontend/index.html")

@app.get("/login")
async def read_login():
    """Serve the login page"""
    return FileResponse("frontend/login.html")

@app.get("/register")
async def read_register():
    """Serve the register page"""
    return FileResponse("frontend/register.html")

@app.get("/search")
async def read_search():
    """Serve the search page"""
    return FileResponse("frontend/search.html")

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "app": settings.APP_NAME}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)

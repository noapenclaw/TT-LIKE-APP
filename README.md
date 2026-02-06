# TT-LIKE-APP 🎵📱

A full-stack TikTok-style short video platform built with **Spring Boot** (backend) and **React Native** (frontend).

## 🌟 Features

### Backend (Spring Boot 3.2 + Java 21)
- ✅ **User Authentication** - JWT-based auth system
- ✅ **Video Upload** - File storage abstraction (S3-ready, local fallback)
- ✅ **For You Page Algorithm** - Smart feed with engagement scoring
- ✅ **Following Feed** - Videos from followed users
- ✅ **Likes & Comments** - Full engagement system with nested replies
- ✅ **Follow System** - Follow/unfollow users
- ✅ **User Profiles** - Videos, followers, following counts
- ✅ **Redis Caching** - Performance optimization
- ✅ **WebSocket Ready** - Real-time notifications structure

### Frontend (React Native 0.73)
- ✅ **Vertical Video Feed** - Auto-play on scroll (TikTok-style)
- ✅ **Double-Tap to Like** - Heart animation on videos
- ✅ **Comment Overlay** - Slide-up comment section
- ✅ **Video Recording** - Camera integration for uploads
- ✅ **User Profiles** - Grid view of user's videos
- ✅ **Navigation** - Bottom tabs + stack navigators
- ✅ **Pull-to-Refresh** - Feed refresh gesture

## 🏗️ Architecture

### Modular Design
Both backend and frontend designed for **plug-and-play extensibility**:

```
backend/
├── config/          # Security, WebSocket, CORS, Storage configs
├── controller/      # REST endpoints
├── service/         # Business logic (easy to swap implementations)
├── repository/      # Data access layer
├── entity/          # JPA entities with proper relationships
├── dto/             # Request/response DTOs
├── security/        # JWT, UserDetails (swappable auth)
└── util/            # VideoProcessor, FileStorage (abstracted)

frontend/
├── components/      # Reusable UI components
├── screens/         # Full page screens
├── navigation/      # Navigation configuration
├── hooks/           # Custom React hooks
├── context/         # Global state (Auth, Feed)
├── api/             # API client and endpoints
└── utils/           # Helpers, formatters, constants
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Node.js 18+
- React Native CLI
- Android Studio (for Android) or Xcode (for iOS)
- PostgreSQL (optional, H2 included for dev)
- Redis (optional, for caching)

### 1. Clone the Repository

```bash
git clone https://github.com/noapenclaw/TT-LIKE-APP.git
cd TT-LIKE-APP
```

### 2. Backend Setup

```bash
cd backend

# Run with H2 (dev mode - no external DB needed)
mvn spring-boot:run

# Or with PostgreSQL
cp .env.example .env
# Edit .env with your DB credentials
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

Backend runs on `http://localhost:8080/api`

#### Backend API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/auth/register` | POST | Register new user |
| `/auth/login` | POST | User login |
| `/auth/refresh` | POST | Refresh JWT token |
| `/videos` | POST | Upload new video |
| `/videos/feed/for-you` | GET | Get personalized feed |
| `/videos/feed/following` | GET | Get following feed |
| `/videos/{id}/like` | POST | Like/unlike video |
| `/videos/{id}/comments` | GET/POST | Get/add comments |
| `/users/{id}` | GET | Get user profile |
| `/users/{id}/follow` | POST | Follow/unfollow user |

### 3. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# iOS (Mac only)
cd ios && pod install && cd ..
npm run ios

# Android
npm run android
```

For Expo (if using Expo):
```bash
npx expo start
```

## ⚙️ Configuration

### Backend Environment Variables

Create `backend/.env`:
```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/ttlikeapp
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=password

# JWT
JWT_SECRET=your-super-secret-key
JWT_EXPIRATION=86400000

# AWS S3 (optional)
AWS_S3_ENABLED=true
AWS_REGION=us-east-1
AWS_S3_BUCKET=your-bucket-name
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key

# Redis (optional)
REDIS_HOST=localhost
REDIS_PORT=6379
```

### Frontend Environment Variables

Create `frontend/.env`:
```env
API_BASE_URL=http://localhost:8080/api
SOCKET_URL=http://localhost:8080
```

## 🐳 Docker Setup

```bash
docker-compose up -d
```

This starts:
- PostgreSQL database
- Redis cache
- Backend API

## 🛠️ Development

### Adding New Features

**Backend:**
1. Add entity in `entity/` package
2. Create repository in `repository/`
3. Implement service in `service/` (interface + impl)
4. Add controller in `controller/`
5. Add DTOs in `dto/request` and `dto/response`

**Frontend:**
1. Create screen in `screens/`
2. Add navigation in `navigation/`
3. Update API calls in `api/`
4. Create/update components in `components/`

### Customization

**Video Storage:**
- Default: Local filesystem
- Swap to S3: Set `AWS_S3_ENABLED=true` in env
- Implement custom: Implement `FileStorageService` interface

**Feed Algorithm:**
- Modify scoring in `FeedAlgorithmService`
- Weights configurable in `application.yml`

**Authentication:**
- Default: JWT
- Swap: Implement custom `UserDetailsService`

## 📁 Project Structure

```
TT-LIKE-APP/
├── backend/
│   ├── src/main/java/com/ttlikeapp/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   └── util/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── README.md
│
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── components/
│   │   ├── context/
│   │   ├── hooks/
│   │   ├── navigation/
│   │   ├── screens/
│   │   └── utils/
│   ├── package.json
│   └── README.md
│
├── docker-compose.yml
├── .env.example
└── README.md
```

## 🔒 Security

- JWT tokens with refresh token rotation
- Password hashing with BCrypt
- CORS configuration
- File upload validation
- SQL injection prevention (JPA/Hibernate)
- XSS protection

## 📱 Screenshots

(Coming soon)

## 🤝 Contributing

1. Fork the repository
2. Create feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open Pull Request

## 📄 License

MIT License - feel free to use for your own projects!

## 🙏 Credits

Built by Nohsen (@noapenclaw) 🤠

---

**Questions?** Open an issue or reach out!

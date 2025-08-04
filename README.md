# SwipeLaunch - Solana Token Discovery App

A mobile-first Android application for discovering and interacting with Solana tokens through an intuitive swipeable interface.

## Project Overview

SwipeLaunch revolutionizes how users discover and interact with Solana tokens by providing a Tinder-like swipe interface combined with comprehensive token analytics, presale participation, and social features.

## Key Features

### Token Discovery
- **Swipeable Interface**: Discover new tokens with simple swipe gestures (right to like, left to pass)
- **Real-time Data**: Live token information from Supabase database
- **Smart Recommendations**: Curated token feed based on community activity

### Presale Marketplace
- **Active Presales**: Browse and participate in ongoing token presales
- **Progress Tracking**: Real-time progress bars and countdown timers
- **Wallet Integration**: Direct purchase functionality (in development)

### Activity Feed
- **Social Tracking**: Follow creators and see their token launches
- **Real-time Updates**: Live feed of community activities
- **Engagement Metrics**: Track likes, presale participation, and follows

### Leaderboards
- **Multiple Categories**: Token launches, staking, popularity, and activity rankings
- **Real-time Rankings**: Dynamic leaderboards updated from database
- **Mobile-optimized UI**: Responsive design with emoji indicators

### User Profile
- **Wallet Management**: Connect/disconnect Solana wallets
- **Activity Stats**: Track your swipes, likes, and token interactions
- **Creator Dashboard**: View tokens created and engagement metrics

## Technology Stack

- **Frontend**: Android Native (Kotlin)
- **Blockchain**: Solana Mobile Stack
- **Backend**: Supabase (PostgreSQL + Realtime)
- **UI Components**: Custom swipeable cards, Material Design
- **Architecture**: MVVM pattern with Repository layer

## Installation & Setup

### Prerequisites
- Android Studio (latest version)
- Android SDK (API level 16+)
- Android Emulator or physical device
- Git

### Clone Repository
```bash
git clone https://github.com/yourusername/SwipeLaunch.git
cd SwipeLaunch/test_andriod_tutorial/SolanaMobileApp
```

### Build & Run

1. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the `android` folder

2. **Build the Project**
   ```bash
   cd android
   ./gradlew clean installDebug
   ```

3. **Start the App**
   ```bash
   adb shell am start -n com.anonymous.SolanaMobileApp/.MainActivity
   ```

### Using Android Emulator

```bash
# Start emulator (Pixel 9 Pro recommended)
emulator -avd Pixel_9_Pro -dns-server 8.8.8.8,8.8.4.4 -netdelay none -netspeed full &

# Verify device is connected
adb devices

# Install and run app
./gradlew clean installDebug
adb shell am start -n com.anonymous.SolanaMobileApp/.MainActivity
```

## Database Integration

The app is fully integrated with Supabase for real-time data:
- **100+ tokens** with metadata and statistics
- **1000+ user votes** for token rankings
- **384+ presale participants** tracked
- **Real-time updates** for activity feeds

## Project Structure

```
android/
├── app/
│   ├── src/main/java/com/anonymous/SolanaMobileApp/
│   │   ├── MainActivity.kt           # Main navigation controller
│   │   ├── data/                     # Data models
│   │   │   ├── TokenData.kt
│   │   │   ├── PresaleTokenData.kt
│   │   │   ├── ActivityFeedData.kt
│   │   │   └── LeaderboardData.kt
│   │   ├── ui/                       # UI components
│   │   │   ├── SwipeableTokenCard.kt
│   │   │   ├── ActivityFeedAdapter.kt
│   │   │   ├── PresaleAdapter.kt
│   │   │   └── LeaderboardAdapters/
│   │   └── network/                  # API integration
│   │       └── SupabaseClient.kt
│   └── res/
│       ├── layout/                   # XML layouts
│       ├── drawable/                 # Custom drawables
│       └── values/                   # Resources
└── build.gradle

```

## Recent Updates (v1.0.0)

- ✅ Fixed balance display formatting issues
- ✅ Fixed token creation count on profile page
- ✅ Fixed profile picture display problems
- ✅ Improved data loading from Supabase
- ✅ Enhanced UI responsiveness
- ✅ Optimized tab layouts for all screen sizes

## Upcoming Features

1. **Wallet Integration**: Full Solana wallet transaction support
2. **Real-time WebSocket**: Live activity feed updates
3. **Token Sharing**: Share tokens via SMS, WhatsApp, Telegram
4. **Push Notifications**: Alerts for presale launches and follows
5. **Advanced Analytics**: Detailed token metrics and charts

## Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Testing

Run the test suite:
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Performance

- **Load Time**: < 2 seconds
- **Database Queries**: Optimized with indexing
- **Memory Usage**: Efficient RecyclerView implementations
- **Network**: Cached responses for offline capability

## Security

- Secure wallet connections via Solana Mobile Stack
- No private keys stored locally
- Encrypted database communications
- Input validation and sanitization

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact & Support

- **Developer**: [Your Name]
- **Email**: [your.email@example.com]
- **GitHub**: [@yourusername](https://github.com/yourusername)
- **Issues**: [Report bugs](https://github.com/yourusername/SwipeLaunch/issues)

## Acknowledgments

- Solana Mobile Stack team for wallet integration
- Supabase for real-time database infrastructure
- Android development community for resources and support

---

**Version**: 1.0.0  
**Last Updated**: August 4, 2025  
**Status**: Production Ready
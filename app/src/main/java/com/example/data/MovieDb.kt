package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val posterUrl: String,
    val ottPlatform: String, // Netflix, Prime Video, JioHotstar, Sony LIV, ZEE5, Aha
    val releaseDate: String, // e.g., "Streaming Now", "Releasing Tomorrow", "June 25, 2026"
    val imdbRating: Double,
    val genre: String,
    val languages: String,
    val runtime: String,
    val synopsis: String,
    val trailerUrl: String,
    val isFeatured: Boolean = false,
    val isUpcoming: Boolean = false,
    val isTrending: Boolean = false,
    val isNewSeries: Boolean = false,
    val isWatchlisted: Boolean = false,
    val reminderSet: Boolean = false
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val movieId: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY id DESC")
    fun getAllMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE isFeatured = 1")
    fun getFeaturedMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE isTrending = 1")
    fun getTrendingMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE isUpcoming = 1")
    fun getUpcomingMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE isNewSeries = 1")
    fun getNewSeries(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE isWatchlisted = 1")
    fun getWatchlistMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Int): Movie?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Update
    suspend fun updateMovie(movie: Movie)

    @Query("UPDATE movies SET isWatchlisted = :watchlisted WHERE id = :id")
    suspend fun updateWatchlistStatus(id: Int, watchlisted: Int)

    @Query("UPDATE movies SET reminderSet = :reminder WHERE id = :id")
    suspend fun updateReminderStatus(id: Int, reminder: Int)

    @Delete
    suspend fun deleteMovie(movie: Movie)

    @Query("DELETE FROM movies")
    suspend fun clearAllMovies()

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Query("DELETE FROM notifications")
    suspend fun clearNotifications()
}

@Database(entities = [Movie::class, NotificationItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ott_updates_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-populate database with default rich movies
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val movieDao = database.movieDao()
                    movieDao.insertMovies(getPrePopulatedMovies())
                    
                    // Add standard notifications
                    movieDao.insertNotification(
                        NotificationItem(
                            title = "Welcome to OTT Updates! 🎬",
                            message = "Stay up to date with the latest Netflix, Prime Video, JioHotstar, Sony LIV, ZEE5, & Aha releases."
                        )
                    )
                    movieDao.insertNotification(
                        NotificationItem(
                            title = "Stranger Things Season 5 Available Soon 💥",
                            message = "Netflix has set the streaming date! Releasing tomorrow. Add to watchlist now."
                        )
                    )
                }
            }
        }
    }
}

class MovieRepository(private val movieDao: MovieDao) {
    val allMovies: Flow<List<Movie>> = movieDao.getAllMovies()
    val featuredMovies: Flow<List<Movie>> = movieDao.getFeaturedMovies()
    val trendingMovies: Flow<List<Movie>> = movieDao.getTrendingMovies()
    val upcomingMovies: Flow<List<Movie>> = movieDao.getUpcomingMovies()
    val newSeries: Flow<List<Movie>> = movieDao.getNewSeries()
    val watchlistMovies: Flow<List<Movie>> = movieDao.getWatchlistMovies()
    val notifications: Flow<List<NotificationItem>> = movieDao.getAllNotifications()

    suspend fun getMovieById(id: Int): Movie? {
        return movieDao.getMovieById(id)
    }

    suspend fun insert(movie: Movie): Long {
        return movieDao.insertMovie(movie)
    }

    suspend fun update(movie: Movie) {
        movieDao.updateMovie(movie)
    }

    suspend fun toggleWatchlist(id: Int, isWatchlisted: Boolean) {
        movieDao.updateWatchlistStatus(id, if (isWatchlisted) 1 else 0)
    }

    suspend fun toggleReminder(id: Int, hasReminder: Boolean) {
        movieDao.updateReminderStatus(id, if (hasReminder) 1 else 0)
    }

    suspend fun delete(movie: Movie) {
        movieDao.deleteMovie(movie)
    }

    suspend fun insertNotification(notification: NotificationItem) {
        movieDao.insertNotification(notification)
    }

    suspend fun clearNotifications() {
        movieDao.clearNotifications()
    }
}

fun getPrePopulatedMovies(): List<Movie> {
    return listOf(
        Movie(
            title = "Squid Game Season 2",
            posterUrl = "https://images.unsplash.com/photo-1626814026160-2237a95fc5a0?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "Netflix",
            releaseDate = "Streaming Now",
            imdbRating = 8.8,
            genre = "Thriller / Drama",
            languages = "Korean, English, Hindi, Tamil",
            runtime = "9 Episodes",
            synopsis = "Three years after winning Squid Game, Player 456 remains determined to find the people behind it and put an end to their vicious sport. Using his fortune, Gi-hun funds his investigation.",
            trailerUrl = "https://www.youtube.com/watch?v=lQBmZBJCYCg",
            isFeatured = true,
            isTrending = true,
            isNewSeries = true
        ),
        Movie(
            title = "Stranger Things Season 5",
            posterUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "Netflix",
            releaseDate = "Releasing Tomorrow",
            imdbRating = 8.9,
            genre = "Sci-Fi / Horror / Teen",
            languages = "English, Hindi, Tamil, Telugu",
            runtime = "8 Episodes",
            synopsis = "The final battle of Hawkins begins. Vecna returns stronger, and Eleven must unite with her friends for one last epic conflict across the Upside Down.",
            trailerUrl = "https://www.youtube.com/watch?v=to7Xb76g_U8",
            isFeatured = true,
            isUpcoming = true,
            isNewSeries = true
        ),
        Movie(
            title = "The Family Man Season 3",
            posterUrl = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "Prime Video",
            releaseDate = "Releasing June 15, 2026",
            imdbRating = 8.7,
            genre = "Action / Spy Thriller / Comedy",
            languages = "Hindi, Tamil, Telugu, English",
            runtime = "10 Episodes",
            synopsis = "Srikant Tiwari is back! Facing a brand new geopolitical cyber-threat emerging from the Northeast region, Srikant must balance home crises and national security once again.",
            trailerUrl = "https://www.youtube.com/watch?v=XatRGut35u0",
            isUpcoming = true,
            isNewSeries = true
        ),
        Movie(
            title = "Kalki 2898 AD",
            posterUrl = "https://images.unsplash.com/photo-1547483238-f400e65ccd56?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "Prime Video",
            releaseDate = "Streaming Now",
            imdbRating = 7.8,
            genre = "Mythology / Sci-Fi / Action",
            languages = "Telugu, Tamil, Hindi, Malayalam, Kannada",
            runtime = "180 min",
            synopsis = "In the post-apocalyptic city of Kasi, a bounty hunter named Bhairava is drawn towards a mysterious pregnant woman, whose child is predicted to change the destiny of the world.",
            trailerUrl = "https://www.youtube.com/watch?v=kQDd1AhGIHk",
            isFeatured = true,
            isTrending = true
        ),
        Movie(
            title = "Manjummel Boys",
            posterUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "JioHotstar",
            releaseDate = "Streaming Now",
            imdbRating = 8.4,
            genre = "Survival Thriller / Friendship",
            languages = "Malayalam, Tamil, Telugu, Kannada, Hindi",
            runtime = "135 min",
            synopsis = "A group of friends on a vacation trip to Kodaikanal face an unexpected challenge when one of them falls deep into the infamous Guna Caves.",
            trailerUrl = "https://www.youtube.com/watch?v=tT8s8zWeU8M",
            isTrending = true
        ),
        Movie(
            title = "Avesham",
            posterUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "Prime Video",
            releaseDate = "Streaming Now",
            imdbRating = 8.1,
            genre = "Action / Comedy / Masala",
            languages = "Malayalam, Tamil, Kannada, Hindi",
            runtime = "161 min",
            synopsis = "Three college students in Bangalore find themselves in deep trouble after an altercation. They seek the help of a quirky local gangster named Ranga to seek revenge.",
            trailerUrl = "https://www.youtube.com/watch?v=A8vGisUeTig",
            isTrending = true
        ),
        Movie(
            title = "Gullak Season 4",
            posterUrl = "https://images.unsplash.com/photo-1513151233558-d860c5398176?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "Sony LIV",
            releaseDate = "Streaming Now",
            imdbRating = 9.2,
            genre = "Family Drama / Comedy",
            languages = "Hindi",
            runtime = "5 Episodes",
            synopsis = "Centering around the Mishra family in a small North Indian town, the fourth season of Gullak captures the bittersweet moments of growing up, parenting, and middle-class life.",
            trailerUrl = "https://www.youtube.com/watch?v=m7p7zDkP-vU",
            isNewSeries = true
        ),
        Movie(
            title = "Pushpa 2: The Rule",
            posterUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "JioHotstar",
            releaseDate = "Releasing June 25, 2026",
            imdbRating = 8.5,
            genre = "Action / Crime Drama",
            languages = "Telugu, Tamil, Hindi, Malayalam, Kannada",
            runtime = "172 min",
            synopsis = "The clash between Pushpa Raj and SP Bhanwar Singh Shekhawat continues, with Pushpa now ruling the red sandalwood smuggling empire.",
            trailerUrl = "https://www.youtube.com/watch?v=1k68c83qAIs",
            isUpcoming = true
        ),
        Movie(
            title = "Unstoppable with NBK",
            posterUrl = "https://images.unsplash.com/photo-1485846234645-a62644f84728?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "Aha",
            releaseDate = "Streaming Now",
            imdbRating = 8.3,
            genre = "Talk Show / Reality",
            languages = "Telugu",
            runtime = "80 min per Episode",
            synopsis = "Nandamuri Balakrishna hosts standard and high-spirited interviews with prominent actors, directors, and politicians, offering unprecedented candor.",
            trailerUrl = "https://www.youtube.com/watch?v=7u3Ssk8A96g",
            isTrending = true,
            isNewSeries = true
        ),
        Movie(
            title = "Saindhav",
            posterUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "ZEE5",
            releaseDate = "Streaming Now",
            imdbRating = 6.8,
            genre = "Action Thriller / Drama",
            languages = "Telugu, Tamil, Hindi, Kannada",
            runtime = "140 min",
            synopsis = "A devoted father with a dark past will do whatever it takes to secure extremely expensive medicine for his terminally ill daughter, leading to a clash with underground cartels.",
            trailerUrl = "https://www.youtube.com/watch?v=B70eREm0f90"
        ),
        Movie(
            title = "Ayalaan",
            posterUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=600&auto=format&fit=crop",
            ottPlatform = "ZEE5",
            releaseDate = "Streaming Now",
            imdbRating = 6.4,
            genre = "Sci-Fi / Comedy",
            languages = "Tamil, Telugu, Kannada",
            runtime = "155 min",
            synopsis = "A lost extraterrestrial who lands on Earth befriended by a kind-hearted man, helping him combat a greedy industrialist planning to destroy the planet.",
            trailerUrl = "https://www.youtube.com/watch?v=cM5q0oIqMpw"
        )
    )
}

package com.example.musicstream

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.musicstream.databinding.ActivityPlayerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private lateinit var exoPlayer: ExoPlayer
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = "user123" // Replace with actual user ID (e.g., from FirebaseAuth)
    private var isLiked = false  // Flag to track if the song is liked

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            showGif(isPlaying)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        MyExoplayer.getCurrentSong()?.apply {
            binding.songTitleTextView.text = title
            binding.songSubtitleTextView.text = subtitle
            Glide.with(binding.songCoverImageView).load(coverUrl)
                .circleCrop()
                .into(binding.songCoverImageView)
            Glide.with(binding.songGifImageView).load(R.drawable.media_playing)
                .circleCrop()
                .into(binding.songGifImageView)
            exoPlayer = MyExoplayer.getInstance()!!
            binding.playerView.player = exoPlayer
            binding.playerView.showController()
            exoPlayer.addListener(playerListener)
        }

        // Set up heart icon click listener
        binding.heartIcon.setOnClickListener {
            val currentSongId = MyExoplayer.getCurrentSong()?.id // Assuming song has a unique ID
            if (currentSongId != null) {
                handleLike(currentSongId)
            }
        }

        // Initial setup of the heart icon based on the song's like status
        updateHeartIcon()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.removeListener(playerListener)
    }

    private fun showGif(show: Boolean) {
        if (show) {
            binding.songGifImageView.visibility = View.VISIBLE
        } else {
            binding.songGifImageView.visibility = View.INVISIBLE
        }
    }

    private fun handleLike(songId: String) {
        val currentUser  = FirebaseAuth.getInstance().currentUser
        if (currentUser  != null) {
            // Fetch the username from Firestore
            firestore.collection("users")
                .document(currentUser .uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("username") ?: "Unknown User"

                        // Now add the like and username to the song's likes collection
                        val likeRef = firestore.collection("songs")
                            .document(songId)
                            .collection("likes")
                            .document(currentUser .uid)

                        likeRef.get().addOnSuccessListener { likeDocument ->
                            if (!likeDocument.exists()) {
                                // User hasn't liked the song, add like with the username
                                val userData = hashMapOf("username" to userName)
                                likeRef.set(userData).addOnSuccessListener {
                                    Toast.makeText(this, "Liked the song!", Toast.LENGTH_SHORT).show()
                                    isLiked = true // Update the like flag
                                    updateHeartIcon()  // Change heart icon to red
                                    showUsersWhoLiked(songId) // Show updated list of users
                                }.addOnFailureListener { e ->
                                    Toast.makeText(this, "Error adding like: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // User has already liked the song, remove the like
                                likeRef.delete().addOnSuccessListener {
                                    Toast.makeText(this, "Unliked the song!", Toast.LENGTH_SHORT).show()
                                    isLiked = false // Update the like flag to false (unlike)
                                    updateHeartIcon()  // Change heart icon to white
                                }.addOnFailureListener { e ->
                                    Toast.makeText(this, "Error removing like: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Error checking like: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "User  profile not found", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "User  not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to change heart icon based on the like status
    private fun updateHeartIcon() {
        if (isLiked) {
            binding.heartIcon.setImageResource(R.drawable.ic_heart_filled) // Set red heart icon
        } else {
            binding.heartIcon.setImageResource(R.drawable.ic_heart_outline) // Set white heart icon
        }
    }

    private fun showUsersWhoLiked(songId: String) {
        firestore.collection("songs")
            .document(songId)
            .collection("likes")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val userList = querySnapshot.documents.map {
                    it.getString("username") ?: "Unknown User" // Get the actual username
                }
                if (userList.isNotEmpty()) {
                    // Show the list of users who liked the song
                    showUserListDialog(userList)
                } else {
                    Toast.makeText(this, "No users have liked this song yet", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching likes: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun showUserListDialog(userList: List<String>) {
        val dialog = UserListDialogFragment.newInstance(userList)
        dialog.show(supportFragmentManager, "UserListDialog")
    }
}
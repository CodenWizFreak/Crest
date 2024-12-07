package com.example.musicstream

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.musicstream.databinding.FragmentUserListDialogBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserListDialogFragment : DialogFragment() {

    private var _binding: FragmentUserListDialogBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserListDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userList = arguments?.getStringArrayList(ARG_USER_LIST) ?: arrayListOf()

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, userList)
        binding.userListView.adapter = adapter

        // Set up item click listener for usernames
        binding.userListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val username = userList[position]

            // Intent to open another app (App B) when a username is clicked
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("myappb://open?user=$username") // You can pass username as a parameter
            }
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "App B is not installed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_USER_LIST = "user_list"

        fun newInstance(userList: List<String>): UserListDialogFragment {
            val fragment = UserListDialogFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_USER_LIST, ArrayList(userList))
            fragment.arguments = args
            return fragment
        }
    }
}

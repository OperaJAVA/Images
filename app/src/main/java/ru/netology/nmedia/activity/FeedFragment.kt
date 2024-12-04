package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.RetryTypes
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    private val viewModelAuth: AuthViewModel by viewModels(ownerProducer = ::requireParentFragment)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                if (viewModelAuth.authorized) {
                    if (post.likedByMe)
                        viewModel.unlikeById(post.id)
                    else
                        viewModel.likeById(post.id)
                } else
                    findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onImage(image: String) {
                val bundle = Bundle().apply {
                    putString("image", image)
                }
                findNavController().navigate(
                    R.id.action_feedFragment_to_imageFragment, bundle
                )
            }
        })

        binding.container.adapter = adapter

        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.emptyText.isVisible = state.empty
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
            binding.swipeRefresh.isRefreshing = state.refreshing

            if (state.error) {
                Snackbar.make(
                    binding.root,
                    R.string.error_loading,
                    BaseTransientBottomBar.LENGTH_LONG
                ).setAction(R.string.retry_loading) {
                    when (state.retryType) {
                        RetryTypes.LIKE -> viewModel.likeById(state.retryId)
                        RetryTypes.UNLIKE -> viewModel.unlikeById(state.retryId)
                        RetryTypes.SAVE -> viewModel.retrySave(state.retryPost)
                        RetryTypes.REMOVE -> viewModel.removeById(state.retryId)
                        else -> viewModel.loadPosts()
                    }
                }
                    .show()
            }
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            if (it > 0) {
                binding.newPosts.text = getString(R.string.new_posts)
                binding.newPosts.visibility = View.VISIBLE
            }
            println("Newer count: $it")
        }

        binding.newPosts.setOnClickListener {
            viewModel.loadNewPosts()
            binding.container.smoothScrollToPosition(0)
            binding.newPosts.visibility = View.GONE

        }

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.addPost.setOnClickListener {
            if (viewModelAuth.authorized) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else findNavController().navigate(R.id.action_feedFragment_to_signUpFragment)
        }

        binding.swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_dark
        )

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
        }
        return binding.root
    }
}
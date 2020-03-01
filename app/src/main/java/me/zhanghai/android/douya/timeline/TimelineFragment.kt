/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import me.zhanghai.android.douya.R
import me.zhanghai.android.douya.arch.viewModels
import me.zhanghai.android.douya.databinding.TimelineFragmentBinding
import me.zhanghai.android.douya.util.MergeAdapter
import me.zhanghai.android.douya.util.MoreItemAdapter
import me.zhanghai.android.douya.util.OnVerticalScrollListener
import me.zhanghai.android.douya.util.getInteger
import me.zhanghai.android.douya.util.showToast

class TimelineFragment : Fragment() {
    private val args: TimelineFragmentArgs by navArgs()

    private val viewModel: TimelineViewModel by viewModels { { TimelineViewModel(args.userId) } }

    private lateinit var binding: TimelineFragmentBinding

    private val timelineAdapter = TimelineAdapter()
    private val moreItemAdapter = MoreItemAdapter(timelineAdapter, R.layout.timeline_more_item)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = TimelineFragmentBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.timelineRecycler.run {
            layoutManager = StaggeredGridLayoutManager(
                context.getInteger(R.integer.list_card_column_count), RecyclerView.VERTICAL
            )
            itemAnimator = DefaultItemAnimator().apply { supportsChangeAnimations = false }
            adapter = MergeAdapter(timelineAdapter, moreItemAdapter)
            addOnScrollListener(object : OnVerticalScrollListener() {
                override fun onScrolledToBottom() {
                    viewModel.loadMore()
                }
            })
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.timelineAndDiffResult.observe(viewLifecycleOwner) { (timeline, diffResult) ->
            timelineAdapter.items = timeline
            diffResult?.dispatchUpdatesTo(timelineAdapter)
        }
        viewModel.moreLoading.observe(viewLifecycleOwner) { moreItemAdapter.loading = it }
        viewModel.errorEvent.observe(viewLifecycleOwner) { showToast(it) }
    }
}

package com.baiganov.fintech.presentation.ui.channels.streams

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.baiganov.fintech.App
import com.baiganov.fintech.R
import com.baiganov.fintech.presentation.model.ItemFingerPrint
import com.baiganov.fintech.presentation.model.StreamFingerPrint
import com.baiganov.fintech.presentation.model.TopicFingerPrint
import com.baiganov.fintech.presentation.ui.channels.SearchQueryListener
import com.baiganov.fintech.presentation.ui.channels.streams.CreateStreamDialog.Companion.CREATE_STREAM_REQUEST_KEY
import com.baiganov.fintech.presentation.ui.channels.streams.CreateStreamDialog.Companion.DESCRIPTION_RESULT_KEY
import com.baiganov.fintech.presentation.ui.channels.streams.CreateStreamDialog.Companion.NAME_RESULT_KEY
import com.baiganov.fintech.presentation.ui.channels.streams.recyclerview.ExpandableAdapter
import com.baiganov.fintech.presentation.ui.chat.ChatActivity
import com.baiganov.fintech.presentation.ui.chat.recyclerview.ItemClickListener
import com.baiganov.fintech.presentation.ui.chat.recyclerview.TypeItemClickStream
import com.baiganov.fintech.util.Event
import com.baiganov.fintech.util.State
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider
import androidx.recyclerview.widget.DividerItemDecoration


class StreamsFragment : MvpAppCompatFragment(), StreamsView, ItemClickListener,
    SearchQueryListener {

    private lateinit var rvStreams: RecyclerView
    private lateinit var frameNotResult: LinearLayout
    private lateinit var adapterStreams: ExpandableAdapter
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var btnCreateStream: FloatingActionButton
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    @Inject
    lateinit var presenterProvider: Provider<StreamsPresenter>

    private val presenter: StreamsPresenter by moxyPresenter { presenterProvider.get() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(CREATE_STREAM_REQUEST_KEY) { _, bundle ->
            val streamName = bundle.getString(NAME_RESULT_KEY) as String
            val streamDescription = bundle.getString(DESCRIPTION_RESULT_KEY) as String
            presenter.obtainEvent(Event.EventChannels.CreateStream(streamName, streamDescription))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as App).component.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_streams, container, false)
        rvStreams = view.findViewById(R.id.rv_stream)
        frameNotResult = view.findViewById(R.id.no_result_found)
        shimmer = view.findViewById(R.id.shimmer_streams)
        btnCreateStream = view.findViewById(R.id.btn_create_stream)
        swipeRefreshLayout = view.findViewById(R.id.swipe_container)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.init(requireArguments().getInt(ARG_TAB_POSITION))

        btnCreateStream.setOnClickListener {
            CreateStreamDialog.newInstance().show(parentFragmentManager, null)
        }

        swipeRefreshLayout.setOnRefreshListener {
            presenter.obtainEvent(Event.EventChannels.GetStreams)
        }

        adapterStreams = ExpandableAdapter(this)
        rvStreams.adapter = adapterStreams

        rvStreams.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            ).apply {
                setDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.shape_line
                    )!!
                )
            })
    }

    override fun onItemClick(click: TypeItemClickStream) {
        when (click) {
            is TypeItemClickStream.OpenStream -> {
                startActivity(ChatActivity.createIntent(requireContext(), click.stream))
            }

            is TypeItemClickStream.ClickSteam -> {
                val item = click.item
                val position = click.position
                when (item) {
                    is StreamFingerPrint -> {
                        if (item.isExpanded) {
                            presenter.obtainEvent(
                                Event.EventChannels.OpenStream(
                                    position,
                                    item.childTopics
                                )
                            )
                        } else {
                            presenter.obtainEvent(
                                Event.EventChannels.CloseStream(
                                    item.childTopics
                                )
                            )
                        }
                    }
                    is TopicFingerPrint -> {
                        startActivity(ChatActivity.createIntent(requireContext(), item))
                    }
                }
            }
        }

    }

    override fun render(state: State<List<ItemFingerPrint>>) {
        when (state) {
            is State.Result -> {
                adapterStreams.dataOfList = state.data
                swipeRefreshLayout.isRefreshing = false
                frameNotResult.isVisible = state.data.isEmpty()

                shimmer.isVisible = false
            }
            is State.Loading -> {
                frameNotResult.isVisible = false
                if (adapterStreams.itemCount == 0) {
                    shimmer.isVisible = true
                }
            }
            is State.Error -> {
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                frameNotResult.isVisible = false
                shimmer.isVisible = false
            }
            else -> {

            }
        }
    }

    override fun search(searchQuery: String) {
        presenter.obtainEvent(
            Event.EventChannels.SearchStreams(
                searchQuery
            )
        )
    }

    companion object {

        private const val ARG_TAB_POSITION = "arg_tab_position"

        fun newInstance(tabPosition: Int) =
            StreamsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TAB_POSITION, tabPosition)
                }
            }
    }
}
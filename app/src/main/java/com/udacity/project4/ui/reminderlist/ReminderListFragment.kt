package com.udacity.project4.ui.reminderlist

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.activity.addCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentReminderListBinding
import com.udacity.project4.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReminderListFragment : BaseFragment() {
    private lateinit var binding: FragmentReminderListBinding
    private lateinit var dialog: AlertDialog
    override val _viewModel: RemindersListViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
             requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReminderListBinding.inflate(layoutInflater)
        binding.viewModel = _viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.fabAddReminder.setOnClickListener {
            _viewModel.navigationCommand.value=NavigationCommand.To(ReminderListFragmentDirections.toSaveReminderFragment())
        }
        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadReminders()
        }
        setupMenu()
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onPrepareMenu(menu: Menu) {
                // Handle for example visibility of menu items
            }

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.logout_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.authenticationFragment) {
                    showConfirmationDialog()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showConfirmationDialog() {
        dialog = AlertDialog.Builder(requireContext()).apply {
            setIcon(R.drawable.attention)
            setTitle("Warning!")
            setMessage("Are you sure you want to logout?")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            setPositiveButton("Yes") { _, _ ->
                AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {
                    findNavController().navigate(R.id.authenticationFragment)
                }
            }
        }.show()
    }
}
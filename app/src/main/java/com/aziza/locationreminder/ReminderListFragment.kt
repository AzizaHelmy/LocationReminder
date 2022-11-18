package com.aziza.locationreminder

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.aziza.locationreminder.databinding.FragmentReminderListBinding
import com.firebase.ui.auth.AuthUI

class ReminderListFragment : Fragment() {
    private lateinit var binding: FragmentReminderListBinding
    private lateinit var dialog: AlertDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReminderListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        showAlertDialog()
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
                AuthUI.getInstance().signOut(requireContext()).addOnSuccessListener {
                    findNavController().navigate(R.id.authenticationFragment)
                }
                //showAlertDialog()
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showAlertDialog() {
        dialog = AlertDialog.Builder(requireContext()).apply {
            setIcon(R.drawable.ic_logout)
            setMessage("Logout?")
            setTitle("Confirm")
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            setNegativeButton("Yes") { _, _ ->
                findNavController().navigate(R.id.authenticationFragment)
            }
        }.create()
    }
}
@file:Suppress("ClassName")

package com.billin.com.playground

import android.content.Context
import androidx.navigation.*
import androidx.navigation.fragment.fragment

object nav_graph {

    private var index = 1
    private val nextIndex get() = index++
    private val baseUrl = "billin://playground"

    val id = nextIndex

    object dest {
        val home = nextIndex
        val crash = nextIndex
    }

    object action {
        val home_to_crash = nextIndex
    }


    private fun NavDestinationBuilder<NavDestination>.defaultAction(
        actionId: Int,
        actionBuilder: NavActionBuilder.() -> Unit
    ) = action(actionId) {
        actionBuilder()
        navOptions {
            anim {
                enter = android.R.anim.fade_in
                exit = android.R.anim.fade_out
                popEnter = android.R.anim.fade_in
                popExit = android.R.anim.fade_out
            }
        }
    }

    fun create(
        context: Context,
        navController: NavController
    ) = navController.createGraph(id, dest.home) {

        fragment<HomeFragment>(dest.home) {
            label = context.getString(R.string.fragment_name_home)
            defaultAction(action.home_to_crash) {
                destinationId = dest.crash
            }
        }

        fragment<CrashFragment>(dest.crash) {
            label = context.getString(R.string.fragment_name_crash)
            deepLink("$baseUrl/crash")
        }

    }
}

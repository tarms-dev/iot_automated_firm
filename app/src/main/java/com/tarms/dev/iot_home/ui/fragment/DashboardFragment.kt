package com.tarms.dev.iot_home.ui.fragment


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.tarms.dev.iot_home.R
import com.tarms.dev.iot_home.data.Firm
import com.tarms.dev.iot_home.databinding.FragmentDashboardBinding
import com.tarms.dev.iot_home.repository.MyViewModel
import com.tarms.dev.iot_home.service.ClickEventListener
import com.tarms.dev.iot_home.service.TAG
import com.tarms.dev.iot_home.utils.FirebaseUtil
import com.tarms.dev.iot_home.utils.Utils
import java.util.logging.Logger

class DashboardFragment : Fragment(), ClickEventListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var myViewModel: MyViewModel
    private lateinit var firm: Firm
    private var key = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()

        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)

        val binding = DataBindingUtil.inflate<FragmentDashboardBinding>(
            inflater,
            R.layout.fragment_dashboard,
            container,
            false
        ).apply {
            this.lifecycleOwner = this@DashboardFragment
            this.firm = myViewModel
            this.clickHandler = this@DashboardFragment
        }

        return binding.root
    }

    @SuppressLint("FragmentLiveDataObserve")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myViewModel.getCurrentData().observe(this, Observer { firm ->
            this.firm = firm
        })

        FirebaseUtil.getLatestData { firm, key ->
            myViewModel.updateCurrentData(firm)
            this.key = key
        }
    }

    override fun onViewClick(view: View) {
        if (key.isEmpty() || auth.uid == null) {
            if (auth.uid == null)
                Toast.makeText(context, "Login First!", Toast.LENGTH_SHORT).show()
            return
        }

        Logger.getLogger("onViewClick").warning("KEY: $key")

        val mRef = FirebaseDatabase.getInstance()
            .reference.child(Utils.firmRef(auth.uid.toString()))
            .child(key)

        when (view.id) {
            R.id.light -> try {
                firm.light.l_status = !firm.light.l_status!!
            } catch (e: UninitializedPropertyAccessException) {
                e.printStackTrace()
            }

            R.id.pump -> try {
                firm.pump.p_status = !firm.pump.p_status!!
            } catch (e: UninitializedPropertyAccessException) {
                e.printStackTrace()
            }
        }

        mRef.setValue(firm).addOnCompleteListener {
            if (it.isSuccessful) {
                println("<=================Switch state changed!!=================>")
            }
        }
    }
}

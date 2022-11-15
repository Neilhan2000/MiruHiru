package com.neil.miruhiru.customdetail

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.neil.miruhiru.custom.CustomTypeFilter
import com.neil.miruhiru.custom.item.MyCustomFragment
import com.neil.miruhiru.custom.item.StartCustomFragment
import com.neil.miruhiru.customdetail.item.BottomSheetViewModel
import com.neil.miruhiru.customdetail.item.BottomStepFragment

class BottomSheetPageAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return CustomTypeFilter.values().size
    }

    override fun createFragment(position: Int): Fragment {
        return BottomStepFragment(BottomSheetTypeFilter.values()[position],)
    }

}
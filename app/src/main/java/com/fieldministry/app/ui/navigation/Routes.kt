package com.fieldministry.app.ui.navigation

import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val DASHBOARD = "dashboard"
    const val HOUSEHOLDER_LIST = "householders"
    const val SEARCHING_NEW = "searching/new"
    const val BIBLE_STUDY_NEW = "bible-study/new"
    const val RETURN_VISIT_NEW = "return-visit/new"
    const val ADMIN_HOME = "admin"
    const val ADMIN_PENDING_APPROVALS = "admin/pending-approvals"
    const val ADMIN_MANAGE_USERS = "admin/manage-users"
    const val ADMIN_MANAGE_MUNICIPALITIES = "admin/manage-municipalities"
    const val ADMIN_MANAGE_BARANGAYS = "admin/manage-barangays"
    const val ADMIN_MANAGE_PH_REGIONS = "admin/manage-ph-regions"
    const val ADMIN_MANAGE_PH_PROVINCES = "admin/manage-ph-provinces"
    const val ADMIN_MANAGE_PH_MUNICIPALITIES = "admin/manage-ph-municipalities"
    const val ADMIN_MANAGE_PH_BARANGAYS = "admin/manage-ph-barangays"
    const val REPORTS = "reports"

    private const val HOUSEHOLDER_PROFILE_BASE = "householders/profile"
    const val HOUSEHOLDER_PROFILE_PATTERN = "$HOUSEHOLDER_PROFILE_BASE/{uuid}"

    fun householderProfile(uuid: String): String {
        val encoded = URLEncoder.encode(uuid, "UTF-8")
        return "$HOUSEHOLDER_PROFILE_BASE/$encoded"
    }

    fun decode(value: String): String = URLDecoder.decode(value, "UTF-8")
}

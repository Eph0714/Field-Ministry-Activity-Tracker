package com.fieldministry.app.data.remote

import com.fieldministry.app.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth ----------------------------------------------------------
    @POST("auth/signup")
    suspend fun signup(@Body request: SignUpRequest): ApiMessage

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("auth/me")
    suspend fun me(): UserDto

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ApiMessage

    @Multipart
    @POST("auth/photo")
    suspend fun uploadMyPhoto(@Part photo: MultipartBody.Part): Map<String, String>

    // Municipalities / Barangays -------------------------------------
    @GET("municipalities")
    suspend fun getMunicipalities(): List<MunicipalityDto>

    @POST("municipalities")
    suspend fun createMunicipality(@Body body: Map<String, String>): MunicipalityDto

    @PUT("municipalities/{id}")
    suspend fun updateMunicipality(@Path("id") id: Int, @Body body: Map<String, String>): MunicipalityDto

    @DELETE("municipalities/{id}")
    suspend fun deleteMunicipality(@Path("id") id: Int): Response<Unit>

    @GET("barangays")
    suspend fun getBarangays(@Query("municipality_id") municipalityId: Int? = null): List<BarangayDto>

    @POST("barangays")
    suspend fun createBarangay(@Body body: Map<String, @JvmSuppressWildcards Any>): BarangayDto

    @PUT("barangays/{id}")
    suspend fun updateBarangay(@Path("id") id: Int, @Body body: Map<String, String>): BarangayDto

    @DELETE("barangays/{id}")
    suspend fun deleteBarangay(@Path("id") id: Int): Response<Unit>

    // Users ------------------------------------------------------------
    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("users/pending-signups")
    suspend fun getPendingSignups(): List<UserDto>

    @POST("users/{id}/approve-signup")
    suspend fun approveSignup(@Path("id") id: Int): UserDto

    @POST("users/{id}/reject-signup")
    suspend fun rejectSignup(@Path("id") id: Int): ApiMessage

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): UserDto

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body request: UpdateUserRequest): UserDto

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    // Householders -------------------------------------------------------
    @GET("householders")
    suspend fun getHouseholders(
        @Query("search") search: String? = null,
        @Query("municipality_id") municipalityId: Int? = null,
        @Query("barangay_id") barangayId: Int? = null,
        @Query("status") status: String? = null,
        @Query("updated_since") updatedSince: String? = null,
    ): List<HouseholderDto>

    @GET("householders/{id}")
    suspend fun getHouseholder(@Path("id") id: Int): HouseholderDto

    @GET("householders/{id}/history")
    suspend fun getHouseholderHistory(@Path("id") id: Int): HouseholderHistoryDto

    @POST("householders")
    suspend fun createHouseholder(@Body request: HouseholderRequest): HouseholderDto

    @PUT("householders/{id}")
    suspend fun updateHouseholder(@Path("id") id: Int, @Body request: HouseholderRequest): HouseholderDto

    @PUT("householders/{id}/potential-rv")
    suspend fun setPotentialRv(@Path("id") id: Int, @Body body: Map<String, Boolean>): HouseholderDto

    @Multipart
    @POST("householders/{id}/photo")
    suspend fun uploadHouseholderPhoto(@Path("id") id: Int, @Part photo: MultipartBody.Part): HouseholderDto

    @DELETE("householders/{id}")
    suspend fun deleteHouseholder(@Path("id") id: Int): Response<Unit>

    // Searching (SRC) ------------------------------------------------------
    @GET("searching")
    suspend fun getSearchingSessions(
        @Query("householder_id") householderId: Int? = null,
        @Query("publisher_id") publisherId: Int? = null,
        @Query("updated_since") updatedSince: String? = null,
    ): List<SearchingSessionDto>

    @POST("searching")
    suspend fun createSearchingSession(@Body request: SearchingSessionRequest): SearchingSessionDto

    @PUT("searching/{id}")
    suspend fun updateSearchingSession(@Path("id") id: Int, @Body request: SearchingSessionRequest): SearchingSessionDto

    @DELETE("searching/{id}")
    suspend fun deleteSearchingSession(@Path("id") id: Int): Response<Unit>

    // Bible Study (BS) -------------------------------------------------
    @GET("bible-studies")
    suspend fun getBibleStudies(
        @Query("householder_id") householderId: Int? = null,
        @Query("publisher_id") publisherId: Int? = null,
        @Query("updated_since") updatedSince: String? = null,
    ): List<BibleStudyDto>

    @POST("bible-studies")
    suspend fun createBibleStudy(@Body request: BibleStudyRequest): BibleStudyDto

    @PUT("bible-studies/{id}")
    suspend fun updateBibleStudy(@Path("id") id: Int, @Body request: BibleStudyRequest): BibleStudyDto

    @DELETE("bible-studies/{id}")
    suspend fun deleteBibleStudy(@Path("id") id: Int): Response<Unit>

    // Return Visits (RV) -------------------------------------------------
    @GET("return-visits")
    suspend fun getReturnVisits(
        @Query("householder_id") householderId: Int? = null,
        @Query("publisher_id") publisherId: Int? = null,
        @Query("updated_since") updatedSince: String? = null,
    ): List<ReturnVisitDto>

    @POST("return-visits")
    suspend fun createReturnVisit(@Body request: ReturnVisitRequest): ReturnVisitDto

    @PUT("return-visits/{id}")
    suspend fun updateReturnVisit(@Path("id") id: Int, @Body request: ReturnVisitRequest): ReturnVisitDto

    @DELETE("return-visits/{id}")
    suspend fun deleteReturnVisit(@Path("id") id: Int): Response<Unit>

    // Reports ------------------------------------------------------------
    @GET("reports/searching-summary")
    suspend fun searchingSummary(
        @Query("publisher_id") publisherId: Int? = null,
        @Query("municipality_id") municipalityId: Int? = null,
        @Query("barangay_id") barangayId: Int? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
    ): List<SearchingSummaryRow>

    @GET("reports/bible-study-summary")
    suspend fun bibleStudySummary(
        @Query("publisher_id") publisherId: Int? = null,
        @Query("municipality_id") municipalityId: Int? = null,
        @Query("barangay_id") barangayId: Int? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
    ): List<BibleStudySummaryRow>

    @GET("reports/return-visit-summary")
    suspend fun returnVisitSummary(
        @Query("publisher_id") publisherId: Int? = null,
        @Query("municipality_id") municipalityId: Int? = null,
        @Query("barangay_id") barangayId: Int? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
    ): List<ReturnVisitSummaryRow>

    @GET("reports/potential-return-visits")
    suspend fun potentialReturnVisits(
        @Query("municipality_id") municipalityId: Int? = null,
        @Query("barangay_id") barangayId: Int? = null,
    ): List<HouseholderDto>

    @GET("reports/summary")
    suspend fun reportsSummary(
        @Query("publisher_id") publisherId: Int? = null,
        @Query("municipality_id") municipalityId: Int? = null,
        @Query("barangay_id") barangayId: Int? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
    ): ReportsSummaryDto
}

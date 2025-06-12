
import com.example.momenttrip.api.NaverPlaceAPI.NaverPlaceApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

    fun create(): NaverPlaceApi {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-NCP-APIGW-API-KEY-ID", "YOUR_CLIENT_ID")
                    .addHeader("X-NCP-APIGW-API-KEY", "YOUR_CLIENT_SECRET")
                    .build()
                chain.proceed(request)
            }.build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverPlaceApi::class.java)
    }
}

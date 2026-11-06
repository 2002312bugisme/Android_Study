package com.zzz.daggerapplication.module;

import com.zzz.daggerapplication.service.ApiService;

import dagger.Module;
import dagger.Provides;
import jakarta.inject.Singleton;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Module
public class NetModule {

    // 第一种是构造函数上声明@Inject
    // 第二种方式告知Dagger，可以通过调用该方法来获取到要注入对象的实例
    @Provides
    public OkHttpClient provideOkHttpClient(){
        return new OkHttpClient.Builder()
                .build();
    }

    @Provides
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl("http://www.google.com")
                .build();
    }

    @Singleton
    @Provides
    public ApiService provideApiService(Retrofit retrofit) {
        return retrofit.create(ApiService.class);
    }


}

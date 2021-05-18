package com.example.birdyapp

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import birdy_grpc.Birdy
import birdy_grpc.MainEndpointGrpc.newBlockingStub
import com.example.birdyapp.features.searching_by_name.model.BirdModel
import com.example.birdyapp.features.sign_up.model.UserFields
import com.google.protobuf.ByteString
import io.grpc.Channel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toSingle
import java.util.*

class Repository(private val channel: Channel) {


    @RequiresApi(Build.VERSION_CODES.N)
    fun findBirdByName(name: String): Single<List<BirdModel>> {
        val blockingStub = newBlockingStub(channel)
        val findBirdRequest = Birdy.FindBirdByNameRequest
            .newBuilder()
            .setName(name)
            .build()
        val birds: Iterator<Birdy.FindBirdByNameResponse> =
            blockingStub.findBirdByName(findBirdRequest)

        val matchedBirds = mutableListOf<BirdModel>()
        birds.forEachRemaining {
            matchedBirds.add(
                BirdModel(
                    it.encInfo.name,
                    it.encInfo.description,
                    it.encInfo.photo.toByteArray()
                )
            )
            Log.d("ptenchick", it.encInfo.photo.toByteArray().size.toString())
            Log.d("ptenchick", it.encInfo.name)
        }
        return matchedBirds.toSingle()
    }

    fun setBirdLocation(
        photo: ByteString,
        lat: Double,
        long: Double,
        finder: String
    ): Single<Birdy.AddBirdWithDataResponse> {
        val blockingStub = newBlockingStub(channel)
        val setLocationRequest =
            Birdy.AddBirdWithDataRequest.newBuilder()
                .setPhoto(photo)
                .setInfo(
                    Birdy.UserBirdInfo.newBuilder()
                        .setFoundPoint(
                            Birdy.UserBirdInfo.Point.newBuilder()
                                .setLatitude(lat)
                                .setLongitude(long)
                                .build()
                        )
                        .setFinderEmail(finder)
                        .setFoundTime(Calendar.getInstance().time.toString())
                ).build()

        return blockingStub.addBirdWithData(setLocationRequest).toBuilder().build().toSingle()
    }

    fun loginUser(
        email: String,
        password: String
    ): Single<Pair<Birdy.LoginResponse.Result, UserFields>> {
        return try {
            val blockingStub = newBlockingStub(channel)
            val loginRequest = Birdy.LoginRequest.newBuilder()
                .setEmail(email)
                .setPassword(password)
                .build()

            val response = blockingStub.loginUser(loginRequest)

            val user = UserFields(
                response.info.firstName,
                response.info.lastName,
                response.info.middleName,
                Date(),
                response.info.city
            )

            (response.result to user).toSingle()
            //response.result.toSingle()
        } catch (e: Exception) {
            e.printStackTrace()
            Single.error(e)
        }
    }

    fun registerUser(
        email: String,
        password: String,
        user: UserFields
    ): Single<Birdy.RegistrationResponse.Result> {
        try {
            val blockingStub = newBlockingStub(channel)

            val request = Birdy.RegistrationRequest.newBuilder()
                .setEmail(email)
                .setPassword(password)
                .setFirstName(user.firstName.value)
                .setLastName(user.lastName.value)
                .setMiddleName(user.middleName.value)
                .setCity(user.city.value)
                .setBirthDate(user.birthdayDate.value.toString())
                .build()

            val response = blockingStub.registerUser(request)
            return response.result.toSingle()

        } catch (e: Exception) {
            e.printStackTrace()
            return Single.error(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getBirdLocations(birdName: String): Single<List<Birdy.UserBirdInfo.Point>> {
        try {
            val blockingStub = newBlockingStub(channel)
            val getLocationsRequest = Birdy.FindBirdCoordinatesByNameRequest
                .newBuilder()
                .setName(birdName)
                .build()
            val response =
                blockingStub.findBirdCoordinatesByName(getLocationsRequest)

            val coordinates = mutableListOf<Birdy.UserBirdInfo.Point>()
            response.forEachRemaining {
                Log.d("coord-latitude", it.info.foundPoint.latitude.toString())
                coordinates.add(it.info.foundPoint)
            }
            return coordinates.toSingle()
        } catch (e: Exception) {
            e.printStackTrace()
            return Single.error(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getUsersByCity(city: String): Single<List<Birdy.UserInfo>> {
        try {
            val blockingStub = newBlockingStub(channel)
            val getUsersRequest = Birdy.FindBoysByCityRequest
                .newBuilder()
                .setCity(city)
                .build()
            val response = blockingStub.bindBoysByCity(getUsersRequest)

            val users = mutableListOf<Birdy.UserInfo>()
            response.forEachRemaining {
                Log.d("coord-latitude", it.firstName)
                users.add(it)
            }
            return users.toSingle()
        } catch (e: Exception) {
            e.printStackTrace()
            return Single.error(e)
        }
    }

    fun updateUserInfo(user: UserFields, email: String, password: String): Completable {
        val blockingStub = newBlockingStub(channel)

        val userInfo = Birdy.UserInfo.newBuilder()
            .setFirstName(user.firstName.value)
            .setLastName(user.lastName.value)
            .setMiddleName(user.middleName.value)
            .setBirthDate(user.birthdayDate.value.toString())
            .setCity(user.city.value)
            .setEmail(email)
            .setPassword(password)
            .build()

        return blockingStub.updateUser(userInfo).toSingle().ignoreElement()
        ///response.result.toSingle()
    }

    fun getBirdsTop(amount: Int): Single<MutableList<Birdy.EncyclopedicBirdInfo>> {
        try {
            val blockingStub = newBlockingStub(channel)
            val getTopRequest = Birdy.GetTopBirdsRequest
                .newBuilder()
                .setCount(amount)
                .build()

            val response = blockingStub.getTopBirds(getTopRequest)

            val birds = mutableListOf<Birdy.EncyclopedicBirdInfo>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                response.forEachRemaining {
                    Log.d("bird-top", it.name)
                    birds.add(it)
                }
            }
            return birds.toSingle()
        } catch (e: Exception) {
            e.printStackTrace()
            return Single.error(e)
        }
    }

    fun birdBySound(
        sound: ByteString,
        lat: Double,
        long: Double,
        finder: String
    ): Single<Birdy.AddBirdWithDataResponse> {
        try {
            val blockingStub = newBlockingStub(channel)
            val setLocationRequest =
                Birdy.AddBirdWithDataRequest.newBuilder()
                    .setSound(sound)
                    .setInfo(
                        Birdy.UserBirdInfo.newBuilder()
                            .setFoundPoint(
                                Birdy.UserBirdInfo.Point.newBuilder()
                                    .setLatitude(lat)
                                    .setLongitude(long)
                                    .build()
                            )
                            .setFinderEmail(finder)
                            .setFoundTime(Calendar.getInstance().time.toString())
                    ).build()

            return blockingStub.addBirdWithData(setLocationRequest).toBuilder().build().toSingle()
        } catch (e: Exception) {
            e.printStackTrace()
            return Single.error(e)
        }
    }

    fun sendResetCode(email: String): Single<Birdy.Empty> {
        return try {
            val blockingStub = newBlockingStub(channel)
            val resetPasswordRequest =
                Birdy.ResetPasswordRequest.newBuilder()
                    .setEmail(email)
                    .build()

            blockingStub.resetPassword(resetPasswordRequest).toBuilder().build().toSingle()

        } catch (e: Exception) {
            e.printStackTrace()
            Single.error(e)
        }
    }

    fun verifyToken(token: String, email: String): Single<Birdy.VerifyTokenResponse> {
        return try {
            val blockingStub = newBlockingStub(channel)
            val verifyTokenRequest =
                Birdy.VerifyTokenRequest.newBuilder()
                    .setToken(token)
                    .setEmail(email)
                    .build()

            blockingStub.verifyToken(verifyTokenRequest).toBuilder().build().toSingle()

        } catch (e: Exception) {
            e.printStackTrace()
            Single.error(e)
        }
    }

}
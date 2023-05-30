package code.sandeep.data.user

interface UserDataSource {

    suspend fun getUserByUserName(userName:String):User?

    suspend fun insertUser(user: User):Boolean

}
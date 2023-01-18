package com.vaani.db

//import android.content.Context
//import android.util.Log
//import com.vaani.models.Favourite
//import com.vaani.models.File
//import com.vaani.models.Folder
//import com.vaani.util.TAG
//import io.realm.kotlin.Realm
//import io.realm.kotlin.RealmConfiguration
//import io.realm.kotlin.UpdatePolicy
//import io.realm.kotlin.ext.query
//import io.realm.kotlin.query      .RealmResults
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//
//object RealmDB : DBOperations {
//    private lateinit var realm : Realm
//    private val scope = CoroutineScope(Dispatchers.IO)
//
//    override fun init(context: Context) {
//         val config = RealmConfiguration.create(
//            setOf(File::class, Folder::class, Favourite::class))
//        realm = Realm.open(config)
//    }
//
//    override fun getFolders(): List<Folder> {
//        return realm.query<Folder>().find()
//    }
//
//    override fun getFolderMediaList(folder: Folder): List<File> {
//        return realm.query<File>("folderId = $0",folder.id).find()
//    }
//
//    override fun getFavourites(): List<Favourite> {
//        return realm.query<Favourite>().find().sortedBy(Favourite::rank)
//    }
//
//    override suspend fun upsertFolderMediaList(folder: Folder, files: List<File>) : List<File> {
//        Log.d(TAG, "upsertFolderMediaList: $folder")
//        return withContext(scope.coroutineContext) {
//            realm.write {
//                val dbFiles = realm.query<File>("folderId = $0",folder.id).find()
//                val newFiles = files - dbFiles.toSet()
//                val existingFiles = mutableListOf<File>()
//                Log.d(TAG, "upsertFolderMediaList: dbFiles= $dbFiles")
//                Log.d(TAG, "upsertFolderMediaList: newFiles= $newFiles")
//                dbFiles.forEach{
//                    if(files.contains(it))
//                        existingFiles.add(it)
//                    else
//                        delete(it)
//                }
//                Log.d(TAG, "upsertFolderMediaList: existingFiles= $existingFiles")
//                newFiles.map {
//                    copyToRealm(it.apply { folderId = folder.id })
//                } + existingFiles
//            }
//        }
//    }
//
//    override suspend fun upsertFavourite(favourite: Favourite) : Favourite {
//        return withContext(scope.coroutineContext){
//            realm.write {
//                copyToRealm(favourite.apply { file = file?.let(::findLatest) },UpdatePolicy.ALL)
//            }
//        }
//    }
//
//    override suspend fun upsertFolders(keys: Set<Folder>): List<Folder> {
//        Log.d(TAG, "upsertFolders: true $keys")
//        return realm.write {
//            val dbFolders = realm.query<Folder>().find()
//            val result = keys.map { folder ->
//                val dbFolder = dbFolders.find { dbFolder -> dbFolder.path == folder.path }
//                dbFolder?.let{
//                    // if folder exists in db update items count field
//                    dbFolder.apply { items = folder.items }
//                } ?: run {
//                    // else write the object
//                    copyToRealm(folder)
//                }
//            }
//            dbFolders.forEach{
//                folder -> if(!result.contains(folder)) {
//                Log.d(TAG, "upsertFolders: delete $folder")
//                    delete(folder)
//                }
//            }
//            result
//        }
//    }
//
//    override fun deleteFavourite(favourite: Favourite) {
//        scope.launch {
//            realm.write {
//                delete(favourite)
//            }
//        }
//    }
//}
package com.example.flying.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.flying.R
import java.io.InputStream

class ObjectEgoFighter(context: Context) {

    private val mTexEgoFighter : Bitmap = BitmapFactory.decodeResource(context.resources, R.raw.ego_figher_tex)
    private val mTexEgoGun : Bitmap = BitmapFactory.decodeResource(context.resources, R.raw.ego_gun_tex)
    private val mTexEgoGun2 : Bitmap = BitmapFactory.decodeResource(context.resources, R.raw.ego_gun_tex)

    var mInstancePositions : MutableList<Float> = arrayListOf(
        0.0f,  -1.0f, -2.0f, 0.0f,
    )

    var mScale = floatArrayOf(
        1.0f, 1.0f, 1.0f, 1.0f,
    )

    var mObjects : MutableList<BaseObject> = arrayListOf()

    init {

        val inStream : InputStream = context.resources.openRawResource(R.raw.ego_fighter)
        var modelReader = ObjectFileReader()

        val models = modelReader.open(inStream)

        var egoFighter = BaseObject()
        egoFighter.initialize(models[0].mVertexCoord, models[0].mVertexNormal, models[0].mVertexTexture, mTexEgoFighter)
        mObjects.add(egoFighter)

        var egoGun1 = BaseObject()
        egoGun1.initialize(models[1].mVertexCoord, models[1].mVertexNormal, models[1].mVertexTexture, mTexEgoGun)
        mObjects.add(egoGun1)

        var egoGun2 = BaseObject()
        egoGun2.initialize(models[2].mVertexCoord, models[2].mVertexNormal, models[2].mVertexTexture, mTexEgoGun2)
        mObjects.add(egoGun2)
    }

    fun draw(mvpMatrix:FloatArray) {
        mObjects[0].mInstancePositions = mInstancePositions
        mObjects[0].mNumInstances = 1
        mObjects[0].draw(mvpMatrix, 0.05f)

        mObjects[1].mInstancePositions = mInstancePositions
        mObjects[1].mNumInstances = 1
        mObjects[1].draw(mvpMatrix, 0.05f)

        mObjects[2].mInstancePositions = mInstancePositions
        mObjects[2].mNumInstances = 1
        mObjects[2].draw(mvpMatrix, 0.05f)
    }


}
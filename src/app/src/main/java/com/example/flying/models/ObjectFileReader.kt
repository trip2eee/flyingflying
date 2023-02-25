package com.example.flying.models

import android.graphics.ColorSpace.Model
import android.util.Log
import com.example.flying.R
import java.io.InputStream

class ModelObject(name: String, vCoord:FloatArray, vNormal:FloatArray, vTexture:FloatArray) {
    public val mName = name
    public val mVertexCoord = vCoord
    public val mVertexNormal = vNormal
    public val mVertexTexture = vTexture
}

class Vertex2D {
    public var u = 0.0f
    public var v = 0.0f
}

class Vertex3D {
    public var x = 0.0f
    public var y = 0.0f
    public var z = 0.0f
}

class ObjectFileReader {
    var mVertexCoord : MutableList<Vertex3D> = arrayListOf()
    var mVertexNormal : MutableList<Vertex3D> = arrayListOf()
    var mVertexTexture : MutableList<Vertex2D> = arrayListOf()

    var mFaceCoord : MutableList<Float> = arrayListOf()
    var mFaceNormal : MutableList<Float> = arrayListOf()
    var mFaceTexture : MutableList<Float> = arrayListOf()

    var mModels : MutableList<ModelObject> = arrayListOf()

    fun open(in_stream: InputStream) : MutableList<ModelObject> {

        var obj_name : String = ""
        mVertexCoord.clear()
        mVertexNormal.clear()
        mVertexTexture.clear()

        in_stream.bufferedReader().forEachLine {
            //Log.d("ofr", it)

            val tokens = it.split(" ").toTypedArray()

            // If not comment
            if(tokens[0] != "#") {

                // If object
                if(tokens[0] == "o"){
                    if(mFaceCoord.size > 0) {
                        val model = ModelObject(obj_name, mFaceCoord.toFloatArray(), mFaceNormal.toFloatArray(), mFaceTexture.toFloatArray())
                        mModels.add(model)
                        mFaceCoord.clear()
                        mFaceNormal.clear()
                        mFaceTexture.clear()
                    }
                    obj_name = tokens[1]
                }
                // vertex coordinate
                else if(tokens[0] == "v"){
                    var v = Vertex3D()
                    v.x = tokens[1].toFloat()
                    v.y = tokens[2].toFloat()
                    v.z = tokens[3].toFloat()
                    mVertexCoord.add(v)
                }
                // vertex normal
                else if(tokens[0] == "vn"){
                    var v = Vertex3D()
                    v.x = tokens[1].toFloat()
                    v.y = tokens[2].toFloat()
                    // to convert blender coordinate to OpenGL coordinate
                    v.z = -tokens[3].toFloat()
                    mVertexNormal.add(v)
                }
                // vertex texture
                else if(tokens[0] == "vt"){
                    var v = Vertex2D()
                    v.u = tokens[1].toFloat()
                    v.v = tokens[2].toFloat()
                    mVertexTexture.add(v)
                }
                // face element
                else if(tokens[0] == "f"){
                    // f v1/t2/n3 v1/t2/n3 v1/t2/n3
                    for(i in 0 until 3) {

                        val idx_vertex = tokens[i+1].split("/")
                        val v = idx_vertex[0].toInt() - 1   // -1 to make zero-based index
                        val t = idx_vertex[1].toInt() - 1
                        val n = idx_vertex[2].toInt() - 1

                        mFaceCoord.add(mVertexCoord[v].x)
                        mFaceCoord.add(mVertexCoord[v].y)
                        mFaceCoord.add(mVertexCoord[v].z)

                        mFaceTexture.add(mVertexTexture[t].u)
                        mFaceTexture.add(mVertexTexture[t].v)

                        mFaceNormal.add(mVertexNormal[n].x)
                        mFaceNormal.add(mVertexNormal[n].y)
                        mFaceNormal.add(mVertexNormal[n].z)
                    }
                }
                else
                {
                    // skip
                }
            }



        }

        if(mFaceCoord.size > 0) {
            val model = ModelObject(obj_name, mFaceCoord.toFloatArray(), mFaceNormal.toFloatArray(), mFaceTexture.toFloatArray())
            mModels.add(model)
        }

        return mModels
    }
}
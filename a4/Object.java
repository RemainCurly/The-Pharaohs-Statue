package a4;

import org.joml.*;

//For each ImportedModel, takes the information, stores it, and creates the values for it via for loop
public class Object
{
    private int numVertices;
    private Vector3f[] vertices;
    private Vector2f[] texCoords;
    private Vector3f[] normals;
    private float[] pValues;
    private float[] tValues;
    private float[] nValues;

    public Object(int numVertices, Vector3f[] vertices, Vector2f[] texCoords, Vector3f[] normals)
    {
        this.numVertices = numVertices;
        this.vertices = vertices;
        this.texCoords = texCoords;
        this.normals = normals;

        pValues = new float[numVertices*3]; //Vertex Positions
        tValues = new float[numVertices*2]; //Texture Coords
        nValues = new float[numVertices*3]; //Normal Vectors

        createValues();
    }

    private void createValues()
    {
        for(int i = 0; i < numVertices; i++)
        {
            pValues[i*3]   = (float) (vertices[i]).x();
            pValues[i*3+1] = (float) (vertices[i]).y();
            pValues[i*3+2] = (float) (vertices[i]).z();
            tValues[i*2]   = (float) (texCoords[i]).x();
            tValues[i*2+1] = (float) (texCoords[i]).y();
            nValues[i*3]   = (float) (normals[i]).x();
            nValues[i*3+1] = (float) (normals[i]).y();
            nValues[i*3+2] = (float) (normals[i]).z();
        }
    }

    public int getNumVertices()
    {
        return numVertices;
    }
    public Vector3f[] getVertices()
    {
        return vertices;
    }
    public Vector2f[] getTexCoords()
    {
        return texCoords;
    }
    public Vector3f[] getNormals()
    {
        return normals;
    }
    public float[] getpValues()
    {
        return pValues;
    }
    public float[] gettValues()
    {
        return tValues;
    }
    public float[] getnValues()
    {
        return nValues;
    }
}

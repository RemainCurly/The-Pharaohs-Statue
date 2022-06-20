package a4;

//This class is mainly used just so i have basic shape hardcoded vertices all in one place
//I only ended up needing "CUBE" for the cubemap
public class BasicShape
{
    private float[] vertices;
    private float[] textureCoords;

    enum Shape
    {
        CUBE, PYRAMID, DIAMOND
    }

    public BasicShape(Shape shape)
    {
        switch(shape)
        {
            case CUBE:
                buildCube();
                break;
            case PYRAMID:
                buildPyramid();
                break;
            case DIAMOND:
                buildDiamond();
                break;
            default:
        }
    }

    public float[] getVertices()
    {
        return vertices;
    }
    public float[] getTextureCoords()
    {
        return textureCoords;
    }

    private void buildCube()
    {
        vertices = new float[]
                {
                        -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f,
                        1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f,
                        -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f,
                        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f,
                        -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
                        1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f
                };

        textureCoords = new float[]
                {
                        0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, //3, 1, 2
                        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, //4, 5, 6
                        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, //1, 2, 3
                        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, //4, 5, 6
                        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, //1, 2, 3
                        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, //4, 5, 6
                        0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, //1, 2, 3
                        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, //4, 5, 6
                        0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, //3, 1, 2
                        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, //4, 5, 6
                        0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, //3, 1, 2
                        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f ///4, 5, 6
                };
    }

    private void buildPyramid()
    {
        vertices = new float[]
                {
                        -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,   //front
                        1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,   //right
                        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f, //back
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f, //left
                        -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
                        1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
                };

        textureCoords = new float[]
                {
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //front
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //right
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //back
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //left
                        0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, //LF
                        1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f  //RR
                };
    }

    private void buildDiamond()
    {
        vertices = new float[]
                {
                        -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front TOP
                        -1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, -3.0f, 0.0f,   //front BOTTOM
                        1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right TOP
                        1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, -3.0f, 0.0f,   //right BOTTOM
                        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back TOP
                        1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, -3.0f, 0.0f, //back BOTTOM
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left TOP
                        -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, -3.0f, 0.0f, //left BOTTOM
                        -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
                        1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
                };

        textureCoords = new float[]
                {
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //front TOP
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //front BOTTOM
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //right TOP
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //right BOTTOM
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //back TOP
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //back BOTTOM
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //left TOP
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //left BOTTOM
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f, //LF
                        0.0f, 0.0f, 1.0f, 0.0f, 0.5f, 1.0f  //RR
                };
    }
}

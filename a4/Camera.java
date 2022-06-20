package a4;

import org.joml.*;
import java.lang.Math;

//Camera object with multiple constructors *fixed from a3*
public class Camera
{
    private Vector3f axisU;
    private Vector3f axisV;
    private Vector3f axisN;
    private Vector4f positionC;
    private Matrix4f rMat;
    private Matrix4f tMat;
    private float scrollAmount;
    private float rotateAmount;

    enum Direction
    {
        FORWARD, BACKWARD, UP, DOWN, LEFT, RIGHT,
        PAN_LEFT, PAN_RIGHT, PITCH_UP, PITCH_DOWN
    }

    public Camera()
    {
        createUVNC();
        refreshMatrices();

        scrollAmount = 0.3f;
        rotateAmount = (float)Math.toRadians(10.0f);
    }

    public Camera(float scrollAmount, float rotateAmount)
    {
        createUVNC();
        refreshMatrices();

        this.scrollAmount = scrollAmount;
        this.rotateAmount = rotateAmount;
    }

    public Camera(Vector3f cameraLocation)
    {
        createUVNC(cameraLocation);
        refreshMatrices();

        scrollAmount = 0.3f;
        rotateAmount = (float)Math.toRadians(10.0f);
    }

    public Camera(Vector3f cameraLocation, float scrollAmount, float rotateAmount)
    {
        createUVNC(cameraLocation);
        refreshMatrices();

        this.scrollAmount = scrollAmount;
        this.rotateAmount = rotateAmount;
    }

    private void createUVNC()
    {
        axisU = new Vector3f(1.0f, 0.0f, 0.0f);
        axisV = new Vector3f(0.0f, 1.0f, 0.0f);
        axisN = new Vector3f(0.0f, 0.0f, 1.0f);
        positionC = new Vector4f(0.0f, 0.0f, 15.0f, 1.0f);
    }

    private void createUVNC(Vector3f cameraLocation)
    {
        axisU = new Vector3f(1.0f, 0.0f, 0.0f);
        axisV = new Vector3f(0.0f, 1.0f, 0.0f);
        axisN = new Vector3f(0.0f, 0.0f, 1.0f);
        positionC = new Vector4f(cameraLocation, 1.0f);
    }

    public void refreshMatrices()
    {
        rMat = new Matrix4f(axisU.x(), axisU.y(), axisU.z(), 0,
                axisV.x(), axisV.y(), axisV.z(), 0,
                axisN.x(), axisN.y(), axisN.z(), 0,
                0.0f, 0.0f, 0.0f, 1.0f);
        rMat.transpose();

        tMat = new Matrix4f(1.0f, 0.0f, 0.0f, -positionC.x(),
                0.0f, 1.0f, 0.0f, -positionC.y(),
                0.0f, 0.0f, 1.0f, -positionC.z(),
                0.0f, 0.0f, 0.0f, 1.0f);
        tMat.transpose();
    }

    public void moveCamera(Direction way)
    {
        switch(way)
        {
            case FORWARD:
                positionC.set(positionC.x()-axisN.x()*scrollAmount, positionC.y()-axisN.y()*scrollAmount, positionC.z() - axisN.z()*scrollAmount, 1.0f);
                break;
            case BACKWARD:
                positionC.set(positionC.x()+axisN.x()*scrollAmount, positionC.y()+axisN.y()*scrollAmount, positionC.z() + axisN.z()*scrollAmount, 1.0f);
                break;
            case LEFT:
                positionC.set(positionC.x() - axisU.x()*scrollAmount, positionC.y()-axisU.y()*scrollAmount, positionC.z()-axisU.z()*scrollAmount, 1.0f);
                break;
            case RIGHT:
                positionC.set(positionC.x() + axisU.x()*scrollAmount, positionC.y()+axisU.y()*scrollAmount, positionC.z()+axisU.z()*scrollAmount, 1.0f);
                break;
            case UP:
                positionC.set(positionC.x()+axisV.x()*scrollAmount, positionC.y() + axisV.y()*scrollAmount, positionC.z()+axisV.z()*scrollAmount, 1.0f);
                break;
            case DOWN:
                positionC.set(positionC.x()-axisV.x()*scrollAmount, positionC.y() - axisV.y()*scrollAmount, positionC.z()-axisV.z()*scrollAmount, 1.0f);
                break;
            case PAN_LEFT:
                axisU.rotateAxis(rotateAmount, 0.0f, axisV.y(), 0.0f);
                axisN.rotateAxis(rotateAmount, 0.0f, axisV.y(), 0.0f);
                axisV.rotateAxis(rotateAmount, 0.0f, axisV.y(), 0.0f);
                break;
            case PAN_RIGHT:
                axisU.rotateAxis(-rotateAmount, 0.0f, axisV.y(), 0.0f);
                axisN.rotateAxis(-rotateAmount, 0.0f, axisV.y(), 0.0f);
                axisV.rotateAxis(-rotateAmount, 0.0f, axisV.y(), 0.0f);
                break;
            case PITCH_UP:
                axisV.rotateAxis(rotateAmount, axisU.x(), axisU.y(), axisU.z());
                axisN.rotateAxis(rotateAmount, axisU.x(), axisU.y(), axisU.z());
                break;
            case PITCH_DOWN:
                axisV.rotateAxis(-rotateAmount, axisU.x(), axisU.y(), axisU.z());
                axisN.rotateAxis(-rotateAmount, axisU.x(), axisU.y(), axisU.z());
                break;
            default:
        }
    }

    public String toString()
    {
        return "CAMERA LOCATED AT: (" + positionC.x() + ", " + positionC.y() + ", " + positionC.z() + ")!";
    }

    public float getScrollAmount()
    {
        return scrollAmount;
    }
    public float getRotateAmount()
    {
        return rotateAmount;
    }
    public void setScrollAmount(float scrollAmount)
    {
        this.scrollAmount = scrollAmount;
    }
    public void setRotateAmount(float rotateAmount)
    {
        this.rotateAmount = rotateAmount;
    }

    public Matrix4f getrMat()
    {
        return rMat;
    }
    public Matrix4f gettMat()
    {
        return tMat;
    }
}

package a4;

import java.awt.*;
import java.util.Random;

//Used to do all the calculations for the Pharaoh's noise texture
public class Noise
{
    private double[][][] noise;
    private Random random = new Random();
    private int noiseWidth;
    private int noiseHeight;
    private int noiseDepth;
    private double veinFrequency;   //Number of stripes
    private double turbPower;       //Amount of perturbation in stripes (0 = unperturbed)
    private double turbSize;        //Zoom factor used when generating turbulence

    public Noise(int noiseWidth, int noiseHeight, int noiseDepth)
    {
        noise = new double[noiseWidth][noiseHeight][noiseDepth];
        this.noiseWidth = noiseWidth;
        this.noiseHeight = noiseHeight;
        this.noiseDepth = noiseDepth;

        veinFrequency = 1.5;
        turbPower = 2.2;
        turbSize = 32.0;

        generateNoise();
    }

    public void fillDataArray(byte[] data)
    {
        for(int i = 0; i < noiseWidth; i++)
        {
            for(int j = 0; j < noiseHeight; j++)
            {
                for(int k = 0; k < noiseDepth; k++)
                {
                    double xyzValue = (float)i/noiseWidth + (float)j/noiseHeight + (float)k/noiseDepth + turbPower
                                        * turbulence(i, j, k, turbSize)/256.0;

                    double sineValue = logistic(Math.abs(Math.sin(xyzValue * 3.14159 * veinFrequency)));
                    sineValue = Math.max(-1.0, Math.min(sineValue * 1.25 - 0.20, 1.0));

                    Color c = new Color((float)Math.min(sineValue*.85f, 1.0), (float)Math.min(sineValue*.4f, 1.0), (float)Math.min(sineValue*.12f, 1.0));

                    data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+0] = (byte) c.getRed();
                    data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+1] = (byte) c.getGreen();
                    data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+2] = (byte) c.getBlue();
                    data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+3] = (byte) 255;
                }
            }
        }
    }

    private double turbulence(double x, double y, double z, double size)
    {
        double value = 0.0, initialSize = size;

        while(size >= 0.9)
        {
            value = value + smoothNoise(x/size, y/size, z/size) * size;
            size /= 2.0;
        }

        value = 128.0 * value / initialSize;
        return value;
    }

    private double smoothNoise(double x1, double y1, double z1)
    {
        double fractX = x1 - (int) x1;
        double fractY = y1 - (int) y1;
        double fractZ = z1 - (int) z1;

        int x2 = ((int)x1 + noiseWidth + 1) % noiseWidth;
        int y2 = ((int)y1 + noiseHeight+ 1) % noiseHeight;
        int z2 = ((int)z1 + noiseDepth + 1) % noiseDepth;

        double value = 0.0;
        value += (1-fractX) * (1-fractY) * (1-fractZ) * noise[(int)x1][(int)y1][(int)z1];
        value += (1-fractX) * fractY     * (1-fractZ) * noise[(int)x1][(int)y2][(int)z1];
        value += fractX     * (1-fractY) * (1-fractZ) * noise[(int)x2][(int)y1][(int)z1];
        value += fractX     * fractY     * (1-fractZ) * noise[(int)x2][(int)y2][(int)z1];

        value += (1-fractX) * (1-fractY) * fractZ     * noise[(int)x1][(int)y1][(int)z2];
        value += (1-fractX) * fractY     * fractZ     * noise[(int)x1][(int)y2][(int)z2];
        value += fractX     * (1-fractY) * fractZ     * noise[(int)x2][(int)y1][(int)z2];
        value += fractX     * fractY     * fractZ     * noise[(int)x2][(int)y2][(int)z2];

        return value;
    }

    private void generateNoise()
    {
        for(int x = 0; x < noiseWidth; x++)
        {
            for(int y = 0; y < noiseHeight; y++)
            {
                for(int z = 0; z < noiseDepth; z++)
                {
                    noise[x][y][z] = random.nextDouble();
                }
            }
        }
    }

    private double logistic(double x)
    {
        double k = 3.0;
        return (1.0/(1.0+Math.pow(2.718, -k * x)));
    }

    public int getNoiseWidth()
    {
        return noiseWidth;
    }
    public int getNoiseHeight()
    {
        return noiseHeight;
    }
    public int getNoiseDepth()
    {
        return noiseDepth;
    }
}

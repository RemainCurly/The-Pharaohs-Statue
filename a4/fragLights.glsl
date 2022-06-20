#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec3 originalVertex;
in vec2 tc;
in float wantLights;
in float isBumpy;
in vec4 shadow_coord;

out vec4 fragColor;

struct PositionalLight
{	
	vec4 ambient;  
	vec4 diffuse;  
	vec4 specular;  
	vec3 position;
};

struct Material
{	
	vec4 ambient;  
	vec4 diffuse;  
	vec4 specular;  
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;

layout(binding=0) uniform sampler2D samp;
layout(binding=1) uniform sampler2DShadow shadowTex;

void main(void)
{	
	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	
	if(isBumpy > 0.5) //Bump Mapping 
	{
		float a = 5.0;
		float b = 90.0;
		float x = originalVertex.x;
		float y = originalVertex.y;
		float z = originalVertex.z;
		N.x = varyingNormal.x + a*sin(b*x);
		N.y = varyingNormal.y + a*sin(b*y);
		N.z = varyingNormal.z + a*sin(b*z);
		N = normalize(N);
	}
	
	vec3 V = normalize(-varyingVertPos);
	vec3 H = normalize(varyingHalfVector);
	//Fog Code
	vec4 fogColor = vec4(.15, .16, .2, 1.0);
	float fogStart = 10.0;
	float fogEnd = 100.0;
	float dist = length(varyingVertPos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	
	float cosTheta = dot(L,N);
	float cosPhi = dot(H,N);
	float notInShadow = textureProj(shadowTex, shadow_coord);
	
	vec4 texel = texture(samp, tc);
	
	if(wantLights > 0.5)
	{
		fragColor = texel * (light.ambient + light.diffuse * max(cosTheta,0.0)
				+ light.specular * pow(max(cosPhi,0.0), material.shininess));
		if(notInShadow == 1.0)
			fragColor += light.diffuse * material.diffuse * max(dot(L,N), 0.0)
					+ light.specular * material.specular
					* pow(max(dot(H,N), 0.0), material.shininess*3.0);
	}
	else
		fragColor = mix(fogColor, texel, fogFactor); //Fog on when lights are off
}
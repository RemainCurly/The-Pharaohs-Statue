#version 430

in vec3 vNormal;		
in vec3 vVertPos;
in vec3 varyingLightDir;
in vec3 varyingHalfVector;	
in vec4 shadow_coord;

out vec4 fragColor;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;		
uniform mat4 shadowMVP;				
layout (binding = 0) uniform samplerCube t;  
layout (binding = 1) uniform sampler2DShadow shadowTex;	
in float wantLights;

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

void main(void)
{
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(vNormal);
	vec3 V = normalize(-vVertPos);
	vec3 H = normalize(varyingHalfVector);
	float cosTheta = dot(L,N);
	float cosPhi = dot(H,N);
	vec3 r = -reflect(normalize(-vVertPos), normalize(vNormal));
	vec4 texel = texture(t,r);
	float notInShadow = textureProj(shadowTex, shadow_coord);
	
	//Fog Code
	vec4 fogColor = vec4(.15, .16, .2, 1.0);
	float fogStart = 10.0;
	float fogEnd = 100.0;
	float dist = length(vVertPos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	
	if(wantLights > 0.5)
	{
		fragColor = texel * (light.ambient + light.diffuse * max(cosTheta,0.0)
				+ light.specular * pow(max(cosPhi,0.0), material.shininess)) * 2.0;
		if(notInShadow == 1.0)
			fragColor += light.diffuse * material.diffuse * max(dot(L,N), 0.0)
					+ light.specular * material.specular
					* pow(max(dot(H,N), 0.0), material.shininess*3.0);				
	}
	else
		fragColor = mix(fogColor, texel, fogFactor); //Fog turns on when lights are out
}
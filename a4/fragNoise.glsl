#version 430

in vec3 varyingNormal;
in vec3 originalPosition;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in float wantLights;
in vec4 shadow_coord;

out vec4 fragColor;

struct PositionalLight
{	vec4 ambient;  
	vec4 diffuse;  
	vec4 specular;  
	vec3 position;
};

struct Material
{	vec4 ambient;  
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

layout (binding=0) uniform sampler3D s;
layout (binding=1) uniform sampler2DShadow shadowTex;

void main(void)
{	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-varyingVertPos);
	vec4 fogColor = vec4(.15, .16, .2, 1.0);
	float fogStart = 10.0;
	float fogEnd = 100.0;
	float dist = length(varyingVertPos.xyz);
	float fogFactor = clamp(((fogEnd-dist)/(fogEnd-fogStart)), 0.0, 1.0);
	vec3 R = normalize(reflect(-L, N));
	float cosTheta = dot(L,N);
	float cosPhi = dot(V,R);
	float notInShadow = textureProj(shadowTex, shadow_coord);
	
	vec4 t = texture(s,originalPosition/3.0 + 0.5);

	if(wantLights > 0.5)
	{
		fragColor = t * (light.ambient + light.diffuse * max(cosTheta,0.0)
				+ light.specular * pow(max(cosPhi,0.0), material.shininess));
		if(notInShadow == 1.0)
			fragColor += light.diffuse * material.diffuse * max(dot(L,N), 0.0)
					+ light.specular * material.specular
					* pow(max(dot(V,R), 0.0), material.shininess*3.0);
	}
	else
	{
		vec4 temp = mix(fogColor, t, fogFactor);
		fragColor = vec4(temp.xyz, 1.0);
	}
}

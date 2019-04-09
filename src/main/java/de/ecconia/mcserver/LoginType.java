package de.ecconia.mcserver;

public enum LoginType
{
	//For don't care hosting:
	Random, //The user may login with any username and receives a random UUID [Unencrypted].
	//For temporary hosting on trust:
	Offline, //Only cached users may join, they may fake the username though [Unencrypted].
	//Normal hosting:
	Online, //Username will be verified over Mojang services [Encrypted].
	//Server-Proxy: BungeeCord
	Bungee, //UUID and player properties will be injected via the Hostname field [Unecrypted].
	//Server-Proxy: Velocity
	Velocity, //Uses velocity protocol to inject user data [Unecrypted]. TODO: Implement.
}

<h1 align="center">
  <br>
  <br>
  Relink
  <br>
  <br>
</h1>
![Link Icon](assets/icon.png)
<p align="center"><a target="_blank" href="https://icons8.com/icon/102433/broken-link">Broken Link</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a></p>
<h4 align="center">
Relink is a utility plugin designed for Velocity, a proxy server for Minecraft.
It will automatically relink players to their previously connected server within the network seamlessly.
</h4>

---

## Installation
1. Download the `relink.jar` file from this repository.
2. Copy the `relink.jar` file into the plugins folder of your velocity network.
3. Restart the Velocity network to load the plugin.

## Configuration
Upon loading the plugin for the first time, a default configuration file (`config.toml`) will be generated in the plugin folder. You can customize this configuration according to your preferences.

### Configuration Options
- **Proxy Servers** (`[proxy]`)
    - Define the list of servers that are linked by Relink.
    - Default values are provided based on the default servers in the network upon first installation.

- **SQL Database Connection** (`[sql]`)
    - Specify the URL, username, and password for the SQL database.
    - This is required for Relink to store and retrieve player connection data.

## Usage
Users need to specify which servers are linked by Relink and provide the SQL database details. Once configured, Relink will seamlessly relink players to their last visited server within the network.

## Contributing
Feel free to contribute to this project by submitting bug reports, feature requests, or pull requests on the [GitHub repository](https://github.com/hello-andrew-yan/relink).
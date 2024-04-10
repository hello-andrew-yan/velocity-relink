<p align="center">
  <img src="assets/icon.png" alt="Link Icon">
</p>
<h1 align="center">
  Relink
  <br>
  <br>
</h1>
<p align="center">
  <a target="_blank" href="https://icons8.com/icon/102433/broken-link">Broken Link</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a></p>
<h4 align="center">
Relink is a utility plugin designed for Velocity, a proxy server for Minecraft.
It will automatically relink players to their previously connected server within the network seamlessly.
</h4>

---

## Installation
1. Download the most recent release of the `relink.jar` file from this repository.
2. Copy the `relink.jar` file into the plugins folder of your velocity network.
3. Restart the Velocity network to load the plugin.

## Configuration
Upon loading the plugin for the first time, a default configuration file (`config.toml`) will be generated in the plugin folder. You can customize this configuration according to your preferences.

### Configuration Options
```toml
#####################################################

[proxy]
# Default values are based on the default servers
# generated when creating new velocity network.
linked = ["lobby", "factions", "minigames"]

#####################################################

[sql]
url = ""
username = ""
password = ""

#####################################################
```
- **Proxy Servers** (`[proxy]`)
    - Define the list of servers that are linked by Relink.
    - Default values are provided based on the default servers in the network upon first installation.

- **SQL Database Connection** (`[sql]`)
    - Specify the URL, username, and password for the SQL database.
    - This is required for Relink to store and retrieve player connection data.

- **Local Storage Alternative**
  - If the user wishes not to connect to a MySQL database, they can leave the values blank in under the `sql` table, and Relink will automatically switch to a local file called `data.toml` stored in the plugin folder.
```toml
# Example data.toml file.
185b9b7a-2cfc-499e-b36a-5e39d6d86e14 = "minigames"
1638e0cf-9204-4b56-b888-07edb1d4a803 = "minigames"
02395569-dc0a-403c-a01d-dbf49e98f855 = "factions"
2137687a-e10d-462b-b574-86d309277146 = "hub"
```

## Usage
Users need to specify which servers are linked by Relink and provide the SQL database details. Once configured, Relink will seamlessly relink players to their last visited server within the network.

## Contributing
Feel free to contribute to this project by submitting bug reports, feature requests, or pull requests on the [GitHub repository](https://github.com/hello-andrew-yan/relink).

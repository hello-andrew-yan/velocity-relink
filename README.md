<p align="center">
  <img src="assets/icon.png" alt="Link Icon">
</p>
<h1 align="center">
  Relink
</h1>
<br>
<p align="center">
  <a href="https://github.com/hello-andrew-yan/relink/commits/master">
    <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/hello-andrew-yan/relink?style=flat-square""></a>
  <a href="https://github.com/hello-andrew-yan/relink/issues">
    <img alt="GitHub issues" src="https://img.shields.io/github/issues-raw/hello-andrew-yan/relink?style=flat-square""></a>
  <a href="https://github.com/hello-andrew-yan/relink/pulls">
    <img alt="GitHub pull requests" src="https://img.shields.io/github/issues-pr-raw/hello-andrew-yan/relink?style=flat-square""></a>
</p>
<p align="center">
  Relink is a utility plugin designed for Velocity, a proxy server for Minecraft.
  It will automatically relink players to their previously connected server within the network seamlessly.
</p>


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
  - If the user wishes not to connect to a MySQL database, they can leave the values blank in under the `[sql]` table, and Relink will automatically switch to a local file called `data.toml` stored in the plugin folder.
```toml
# Example data.toml file.
185b9b7a-2cfc-499e-b36a-5e39d6d86e14 = "minigames"
1638e0cf-9204-4b56-b888-07edb1d4a803 = "minigames"
02395569-dc0a-403c-a01d-dbf49e98f855 = "factions"
2137687a-e10d-462b-b574-86d309277146 = "hub"
```

Users are stored via their UUID's followed by the name of the server under the Velocity network they last connected to before disconnecting.

## Usage
Users need to specify which servers are linked by Relink in the `[proxy]` table accordingly. Once configured, Relink will seamlessly relink players to their last visited server within the network. To retrieve the name of servers in your velocity network, check your `velocity.toml` configuration file in your velocity proxy under the `[servers]` table.

## Contributing
Feel free to contribute to this project by submitting bug reports, feature requests, or pull requests on the [GitHub repository](https://github.com/hello-andrew-yan/relink).

## License
This project is licensed under the [MIT License](LICENSE).

---

<p align="right">
  <a target="_blank" href="https://icons8.com/icon/102433/broken-link">Broken Link</a> icon by <a target="_blank" href="https://icons8.com">Icons8</a>
</p>

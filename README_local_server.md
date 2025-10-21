# Local Codex Server Setup

This setup allows you to host your own codex files instead of relying on `cobblemon.academy`.

## 🚀 Quick Start

1. **Run the setup script**:
   ```bash
   python setup_local_server.py
   ```

2. **Start the server**:
   ```bash
   python codex_server.py
   ```

3. **Test the connection**:
   ```bash
   python local_codex_client.py
   ```

4. **Modify the mod to use your server**:
   ```bash
   python modify_mod.py
   ```

5. **Rebuild the mod**:
   ```bash
   ./gradlew :fabric:build
   ```

## 📁 Files Created

- `codex_server.py` - WebSocket server that mimics cobblemon.academy
- `local_codex_client.py` - Client to test your local server
- `modify_mod.py` - Script to modify AcademyClient.java
- `setup_local_server.py` - Automated setup script
- `start_server.bat` / `start_server.sh` - Startup scripts
- `codex/` - Directory containing your codex files

## 🔧 Configuration

### Server Options

```bash
python codex_server.py --help
```

Options:
- `--host` - Host to bind to (default: localhost)
- `--port` - Port to bind to (default: 8765)
- `--codex-dir` - Directory containing codex files (default: codex)

### Client Options

```bash
python local_codex_client.py --help
```

Options:
- `--server` - WebSocket server URL (default: ws://localhost:8765)

### Mod Modification

```bash
python modify_mod.py --help
```

Options:
- `--server` - Local WebSocket server URL
- `--restore` - Restore original cobblemon.academy URL

## 📦 Codex Files

Place your codex files in the `codex/` directory:

```
codex/
├── assets.zip    # Client-side resources (textures, models, sounds)
└── data.zip      # Server-side data (configs, loot tables, recipes)
```

### Creating Codex Files

1. **Assets.zip**: Contains client-side resources
   - Textures, models, sounds
   - UI elements, shaders
   - Any client-side assets

2. **Data.zip**: Contains server-side data
   - Configurations
   - Loot tables
   - Recipes
   - Any server-side data

## 🌐 Network Configuration

### Local Development
- **Server**: `ws://localhost:8765`
- **Access**: Only from same machine

### Local Network
- **Server**: `ws://0.0.0.0:8765` (bind to all interfaces)
- **Access**: From any device on your network
- **URL**: `ws://YOUR_IP:8765`

### Internet Hosting
- **Server**: `ws://0.0.0.0:8765`
- **Access**: From anywhere on internet
- **URL**: `ws://YOUR_DOMAIN:8765`
- **Requirements**: Port forwarding, domain name, SSL certificate

## 🔒 Security Considerations

- **Authentication**: The server uses simple authentication (easily bypassed)
- **File Access**: Server can read any file in the codex directory
- **Network**: WebSocket connections are not encrypted (use WSS for production)

## 🐛 Troubleshooting

### Connection Issues
- Check if server is running: `python codex_server.py`
- Verify port is not blocked by firewall
- Test with: `python local_codex_client.py`

### File Issues
- Ensure codex files exist in `codex/` directory
- Check file permissions
- Verify zip files are valid

### Mod Issues
- Rebuild mod after modifying AcademyClient.java
- Check mod logs for connection errors
- Verify WebSocket URL is correct

## 📋 Protocol Details

The server implements the same protocol as cobblemon.academy:

1. **Client connects** → Server sends `challenge_auth`
2. **Client responds** → Server sends `complete_auth`
3. **Client requests** → Server sends `update_codex` with files
4. **Files transferred** → Connection can be closed

### Packet Types

- `hello` - Initial connection
- `challenge_auth` - Authentication challenge
- `complete_auth` - Authentication response
- `check_codex` - Request codex files
- `update_codex` - Send codex files
- `update_outfit_tracking` - Outfit updates

## 🔄 Restoring Original Behavior

To restore the mod to use the original cobblemon.academy server:

```bash
python modify_mod.py --restore
./gradlew :fabric:build
```

## 📚 Advanced Usage

### Custom Server URL
```bash
python modify_mod.py --server ws://your-server.com:8765
```

### Multiple Clients
The server supports multiple concurrent connections.

### File Updates
Replace files in `codex/` directory and restart server for updates.

## 🎯 Benefits

- **Offline Development**: Work without internet connection
- **Custom Content**: Host your own assets and data
- **Performance**: Faster downloads from local server
- **Control**: Full control over codex files
- **Testing**: Test mod changes without external dependencies

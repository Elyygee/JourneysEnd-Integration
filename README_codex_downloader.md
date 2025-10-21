# Codex File Downloader

This script downloads the `codex.zip` files from the `cobblemon.academy` WebSocket server, mimicking the behavior of the Academy mod.

## Files Created

- `get_codex_files.py` - Main script to download codex files
- `test_websocket.py` - Simple test script to see WebSocket messages
- `requirements.txt` - Python dependencies

## Setup

1. **Install Python dependencies**:
   ```bash
   pip install -r requirements.txt
   ```

2. **Run the downloader**:
   ```bash
   python get_codex_files.py
   ```

3. **Test WebSocket connection** (optional):
   ```bash
   python test_websocket.py
   ```

## What It Does

The script:

1. **Connects** to `wss://cobblemon.academy/live`
2. **Authenticates** with the server
3. **Calculates checksums** of existing `codex/assets.zip` and `codex/data.zip` files
4. **Requests updates** if checksums don't match
5. **Downloads** the zip files to the `codex/` directory
6. **Saves** files as `codex/assets.zip` and `codex/data.zip`

## Output

The script will create:
```
codex/
├── assets.zip
└── data.zip
```

## Expected Behavior

- ✅ **First run**: Downloads both files
- ✅ **Subsequent runs**: Only downloads if server has newer versions
- ✅ **Authentication**: Handles server authentication automatically
- ✅ **Error handling**: Graceful error handling and logging

## Troubleshooting

- **Connection errors**: Check internet connection and server availability
- **Authentication failures**: Server may have changed authentication requirements
- **File save errors**: Check write permissions in the current directory

## Notes

- The script mimics the exact packet format used by the Academy mod
- Uses the same WebSocket URL: `wss://cobblemon.academy/live`
- Handles Base64 encoded zip file data
- Calculates checksums to avoid unnecessary downloads

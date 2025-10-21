#!/usr/bin/env python3
"""
Modified client to connect to local codex server
"""

import asyncio
import websockets
import json
import base64
import os
import hashlib
from pathlib import Path
import argparse

class LocalCodexClient:
    def __init__(self, server_url="ws://localhost:8765"):
        self.server_url = server_url
        self.codex_dir = Path("codex")
        self.assets_file = self.codex_dir / "assets.zip"
        self.data_file = self.codex_dir / "data.zip"
        self.authenticated = False
        
    def calculate_checksum(self, file_path):
        """Calculate checksum of existing file"""
        if not file_path.exists():
            return None
        try:
            with open(file_path, 'rb') as f:
                content = f.read()
                return hash(content) & 0xFFFFFFFF
        except Exception:
            return None
    
    def create_codex_directory(self):
        """Create codex directory if it doesn't exist"""
        self.codex_dir.mkdir(exist_ok=True)
    
    def save_zip_file(self, data, file_path):
        """Save zip file data to disk"""
        try:
            with open(file_path, 'wb') as f:
                f.write(data)
            print(f"[SUCCESS] Saved {file_path}")
            return True
        except Exception as e:
            print(f"[ERROR] Failed to save {file_path}: {e}")
            return False
    
    async def send_packet(self, websocket, packet_type, **kwargs):
        """Send a packet to the server"""
        packet = {
            "type": packet_type,
            **kwargs
        }
        message = json.dumps(packet)
        print(f"[SEND] Sending: {packet_type}")
        await websocket.send(message)
    
    async def handle_packet(self, websocket, packet):
        """Handle incoming packets from server"""
        packet_type = packet.get("type")
        print(f"[RECEIVE] Received: {packet_type}")
        
        if packet_type == "challenge_auth":
            # Respond to authentication challenge
            server_id = packet.get("server_id")
            print(f"[LOCK] Authentication challenge: {server_id}")
            await self.send_packet(websocket, "complete_auth", success=True)
            
        elif packet_type == "complete_auth":
            # Authentication completed
            success = packet.get("success", False)
            if success:
                print("[SUCCESS] Authentication successful")
                self.authenticated = True
                # Now request codex files
                await self.request_codex_files(websocket)
            else:
                print("[ERROR] Authentication failed")
                
        elif packet_type == "update_codex":
            # Received codex files
            print("[PACKAGE] Received codex files")
            assets_b64 = packet.get("assets")
            data_b64 = packet.get("data")
            
            if assets_b64:
                assets_data = base64.b64decode(assets_b64)
                self.save_zip_file(assets_data, self.assets_file)
            
            if data_b64:
                data_data = base64.b64decode(data_b64)
                self.save_zip_file(data_data, self.data_file)
            
            print("🎉 Codex files downloaded successfully!")
            return True  # Signal completion
            
        elif packet_type == "update_outfit_registry":
            print("👕 Received outfit registry")
            
        else:
            print(f"[QUESTION] Unknown packet type: {packet_type}")
        
        return False
    
    async def request_codex_files(self, websocket):
        """Request codex files from server"""
        print("[CLIPBOARD] Requesting codex files...")
        
        # Calculate checksums of existing files
        assets_checksum = self.calculate_checksum(self.assets_file)
        data_checksum = self.calculate_checksum(self.data_file)
        
        print(f"[CHART] Assets checksum: {assets_checksum}")
        print(f"[CHART] Data checksum: {data_checksum}")
        
        # Send check_codex packet
        await self.send_packet(websocket, "check_codex", 
                             assets_checksum=assets_checksum,
                             data_checksum=data_checksum)
    
    async def connect_and_download(self):
        """Main connection and download logic"""
        print(f"[CONNECT] Connecting to {self.server_url}...")
        
        try:
            async with websockets.connect(self.server_url) as websocket:
                print("[SUCCESS] Connected to WebSocket")
                
                # Send initial hello packet
                await self.send_packet(websocket, "hello")
                
                # Listen for messages
                async for message in websocket:
                    try:
                        packet = json.loads(message)
                        completed = await self.handle_packet(websocket, packet)
                        if completed:
                            break
                    except json.JSONDecodeError as e:
                        print(f"[ERROR] Failed to parse message: {e}")
                        print(f"Raw message: {message}")
                    except Exception as e:
                        print(f"[ERROR] Error handling packet: {e}")
                        
        except websockets.exceptions.ConnectionClosed as e:
            print(f"[CONNECT] Connection closed: {e}")
        except Exception as e:
            print(f"[ERROR] Connection error: {e}")

async def main():
    """Main function"""
    parser = argparse.ArgumentParser(description="Local Codex Client")
    parser.add_argument("--server", default="ws://localhost:8765", help="WebSocket server URL")
    
    args = parser.parse_args()
    
    print("[ROCKET] Starting Local Codex Client")
    print("=" * 50)
    
    client = LocalCodexClient(args.server)
    client.create_codex_directory()
    
    await client.connect_and_download()
    
    print("=" * 50)
    print("[FINISH] Download complete!")

if __name__ == "__main__":
    # Install required package if not available
    try:
        import websockets
    except ImportError:
        print("[ERROR] websockets package not found. Install with:")
        print("pip install websockets")
        exit(1)
    
    asyncio.run(main())

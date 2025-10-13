package abeshutt.staracademy.data.adapter.util;

import abeshutt.staracademy.data.adapter.Adapters;
import abeshutt.staracademy.data.adapter.ISimpleAdapter;
import abeshutt.staracademy.data.bit.BitBuffer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.math.BlockPos;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;

public class BlockPosAdapter implements ISimpleAdapter<BlockPos, NbtElement, JsonElement> {

	private final boolean nullable;

	public BlockPosAdapter(boolean nullable) {
		this.nullable = nullable;
	}

	public boolean isNullable() {
		return this.nullable;
	}

	public BlockPosAdapter asNullable() {
		return new BlockPosAdapter(true);
	}

	@Override
	public void writeBits(BlockPos value, BitBuffer buffer) {
		if (this.nullable) {
			buffer.writeBoolean(value == null);
		}

		if (value != null) {
			buffer.writeLong(value.asLong());
		}
	}

	@Override
	public Optional<BlockPos> readBits(BitBuffer buffer) {
		if (this.nullable && buffer.readBoolean()) {
			return Optional.empty();
		}

		return Optional.of(BlockPos.fromLong(buffer.readLong()));
	}

	@Override
	public void writeBytes(BlockPos value, ByteBuf buffer) {
		if (this.nullable) {
			buffer.writeBoolean(value == null);
		}

		if (value != null) {
			buffer.writeLong(value.asLong());
		}
	}

	@Override
	public Optional<BlockPos> readBytes(ByteBuf buffer) {
		if (this.nullable && buffer.readBoolean()) {
			return Optional.empty();
		}

		return Optional.of(BlockPos.fromLong(buffer.readLong()));
	}

	@Override
	public void writeData(BlockPos value, DataOutput data) throws IOException {
		if (this.nullable) {
			data.writeBoolean(value == null);
		}

		if (value != null) {
			data.writeLong(value.asLong());
		}
	}

	@Override
	public Optional<BlockPos> readData(DataInput data) throws IOException {
		if (this.nullable && data.readBoolean()) {
			return Optional.empty();
		}

		return Optional.of(BlockPos.fromLong(data.readLong()));
	}

	@Override
	public Optional<NbtElement> writeNbt(BlockPos value) {
		if (value == null) {
			return Optional.empty();
		}

		return Optional.of(NbtLong.of(value.asLong()));
	}

	@Override
	public Optional<BlockPos> readNbt(NbtElement nbt) {
		if(nbt == null) {
			return Optional.empty();
		}

		if(nbt instanceof NbtLong value) {
			return Optional.of(BlockPos.fromLong(value.longValue()));
		}

        return Optional.empty();
    }

	@Override
	public Optional<JsonElement> writeJson(BlockPos value) {
        if(value == null) {
			return Optional.empty();
		}

		JsonArray array = new JsonArray();
		Adapters.INT.writeJson(value.getX()).ifPresent(array::add);
		Adapters.INT.writeJson(value.getY()).ifPresent(array::add);
		Adapters.INT.writeJson(value.getZ()).ifPresent(array::add);
        return Optional.of(array);
    }

	@Override
	public Optional<BlockPos> readJson(JsonElement json) {
		if(json instanceof JsonPrimitive primitive) {
			return Adapters.LONG.readJson(primitive).map(BlockPos::fromLong);
		} else if(json instanceof JsonArray array && array.size() == 3) {
			return Optional.of(new BlockPos(
				Adapters.INT.readJson(array.get(0)).orElseThrow(),
				Adapters.INT.readJson(array.get(1)).orElseThrow(),
				Adapters.INT.readJson(array.get(2)).orElseThrow()
			));
		}

		return Optional.empty();
	}
}

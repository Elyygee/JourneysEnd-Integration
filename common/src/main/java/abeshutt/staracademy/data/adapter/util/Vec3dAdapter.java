package abeshutt.staracademy.data.adapter.util;

import abeshutt.staracademy.data.adapter.Adapters;
import abeshutt.staracademy.data.adapter.ISimpleAdapter;
import abeshutt.staracademy.data.bit.BitBuffer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.*;
import net.minecraft.util.math.Vec3d;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Optional;

public class Vec3dAdapter implements ISimpleAdapter<Vec3d, NbtElement, JsonElement> {

	private final boolean nullable;

	public Vec3dAdapter(boolean nullable) {
		this.nullable = nullable;
	}

	public boolean isNullable() {
		return this.nullable;
	}

	public Vec3dAdapter asNullable() {
		return new Vec3dAdapter(true);
	}

	@Override
	public void writeBits(Vec3d value, BitBuffer buffer) {
		if(this.nullable) {
			buffer.writeBoolean(value == null);
		}

		if(value != null) {
			Adapters.DOUBLE.writeBits(value.x, buffer);
			Adapters.DOUBLE.writeBits(value.y, buffer);
			Adapters.DOUBLE.writeBits(value.z, buffer);
		}
	}

	@Override
	public Optional<Vec3d> readBits(BitBuffer buffer) {
		if(this.nullable && buffer.readBoolean()) {
			return Optional.empty();
		}

		return Optional.of(new Vec3d(
			Adapters.DOUBLE.readBits(buffer).orElseThrow(),
			Adapters.DOUBLE.readBits(buffer).orElseThrow(),
			Adapters.DOUBLE.readBits(buffer).orElseThrow()
		));
	}

	@Override
	public void writeBytes(Vec3d value, ByteBuf buffer) {
		if (this.nullable) {
			buffer.writeBoolean(value == null);
		}

		if (value != null) {
			Adapters.DOUBLE.writeBytes(value.x, buffer);
			Adapters.DOUBLE.writeBytes(value.y, buffer);
			Adapters.DOUBLE.writeBytes(value.z, buffer);
		}
	}

	@Override
	public Optional<Vec3d> readBytes(ByteBuf buffer) {
		if (this.nullable && buffer.readBoolean()) {
			return Optional.empty();
		}

		return Optional.of(new Vec3d(
			Adapters.DOUBLE.readBytes(buffer).orElseThrow(),
			Adapters.DOUBLE.readBytes(buffer).orElseThrow(),
			Adapters.DOUBLE.readBytes(buffer).orElseThrow()
		));
	}

	@Override
	public void writeData(Vec3d value, DataOutput data) throws IOException {
		if (this.nullable) {
			data.writeBoolean(value == null);
		}

		if (value != null) {
			Adapters.DOUBLE.writeData(value.x, data);
			Adapters.DOUBLE.writeData(value.y, data);
			Adapters.DOUBLE.writeData(value.z, data);
		}
	}

	@Override
	public Optional<Vec3d> readData(DataInput data) throws IOException {
		if (this.nullable && data.readBoolean()) {
			return Optional.empty();
		}

		return Optional.of(new Vec3d(
			Adapters.DOUBLE.readData(data).orElseThrow(),
			Adapters.DOUBLE.readData(data).orElseThrow(),
			Adapters.DOUBLE.readData(data).orElseThrow()
		));
	}

	@Override
	public Optional<NbtElement> writeNbt(Vec3d value) {
		if(value == null) {
			return Optional.empty();
		}

		NbtList list = new NbtList();
		list.add(NbtDouble.of(value.x));
		list.add(NbtDouble.of(value.y));
		list.add(NbtDouble.of(value.z));
		return Optional.of(list);
	}

	@Override
	public Optional<Vec3d> readNbt(NbtElement nbt) {
		if(nbt == null) {
			return Optional.empty();
		}

		if(nbt instanceof AbstractNbtList<?> list && list.size() == 3) {
			return Optional.of(new Vec3d(
				Adapters.DOUBLE.readNbt(list.get(0)).orElseThrow(),
				Adapters.DOUBLE.readNbt(list.get(1)).orElseThrow(),
				Adapters.DOUBLE.readNbt(list.get(2)).orElseThrow()
			));
		} else if(nbt instanceof NbtCompound compound) {
			return Optional.of(new Vec3d(
				Adapters.DOUBLE.readNbt(compound.get("x")).orElseThrow(),
				Adapters.DOUBLE.readNbt(compound.get("y")).orElseThrow(),
				Adapters.DOUBLE.readNbt(compound.get("z")).orElseThrow()
			));
		}

        return Optional.empty();
    }

	@Override
	public Optional<JsonElement> writeJson(Vec3d value) {
        if(value == null) {
			return Optional.empty();
		}

		JsonArray array = new JsonArray();
		array.add(new JsonPrimitive(value.x));
		array.add(new JsonPrimitive(value.y));
		array.add(new JsonPrimitive(value.z));
		return Optional.of(array);
    }

	@Override
	public Optional<Vec3d> readJson(JsonElement json) {
		if(json instanceof JsonArray array && array.size() == 3) {
			return Optional.of(new Vec3d(
				Adapters.DOUBLE.readJson(array.get(0)).orElseThrow(),
				Adapters.DOUBLE.readJson(array.get(1)).orElseThrow(),
				Adapters.DOUBLE.readJson(array.get(2)).orElseThrow()
			));
		} else if(json instanceof JsonObject object) {
			return Optional.of(new Vec3d(
				Adapters.DOUBLE.readJson(object.get("x")).orElseThrow(),
				Adapters.DOUBLE.readJson(object.get("y")).orElseThrow(),
				Adapters.DOUBLE.readJson(object.get("z")).orElseThrow()
			));
		}


		return Optional.empty();
	}

}

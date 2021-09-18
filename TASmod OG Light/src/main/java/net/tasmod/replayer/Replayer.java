package net.tasmod.replayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import net.minecraft.client.Minecraft;
import net.tasmod.TASmod;
import net.tasmod.random.SimpleRandomMod;
import net.tasmod.random.WeightedRandomMod;
import net.tasmod.virtual.VirtualKeyboard;
import net.tasmod.virtual.VirtualKeyboard.VirtualKeyEvent;
import net.tasmod.virtual.VirtualMouse;
import net.tasmod.virtual.VirtualMouse.VirtualMouseEvent;

/**
 * Records a Speedrun, and saves it into a File.
 * @author Pancake
 */
public final class Replayer {
	
	private final Minecraft mc;
	private final File file;
	private final BufferedReader reader;
	private final Queue<String> linesRead = new LinkedList<String>();
	private final Thread fileReader;
	private int currentTick;
	
	/**
	 * Loads a File and reads some ticks from it
	 * @throws IOException Cannot be thrown, unless something is terribly wrong.
	 */
	public Replayer(File name) throws Exception {
		this.mc = TASmod.mc;
		this.file = name;
		this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.file)));
		
		this.fileReader = new Thread(new Runnable() {
			
			/**
			 * Thread that will read the file and store it in the Queue
			 */
			@Override
			public void run() {
				try {
					while (true) {
						// Only read up to 20 ticks
						if (linesRead.size() < 60) {
							final String line = reader.readLine();
							if (line == null) break;
							linesRead.add(line);
						} else {
							Thread.sleep(32);
						}
					}
					reader.close();
					System.out.println("Read Finished.");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		fileReader.start();
	}
	
	/**
	 * Start the replay
	 */
	public final void startReplay() {
		SimpleRandomMod.updateSeed(0L);
		WeightedRandomMod.intCalls = 0;
		
		VirtualMouse.setCursorPosition(mc.displayWidth / 2, mc.displayHeight / 2);
		VirtualMouse.getDX();
		VirtualMouse.getDY();
	}
	
	/**
	 * Replay Read Ticks
	 */
	public final void tick() {
		tickKeyboad();
		tickMouse();
		SimpleRandomMod.updateSeed(currentTick);
		this.currentTick++;
	}
	
	private final void tickKeyboad() {
		final String line = linesRead.poll();
		if (line != null) {
			if (line.isEmpty()) return;
			final Queue<VirtualKeyEvent> queue = VirtualKeyboard.keyEventsForTick;
			queue.clear();
			Arrays.asList(line.split(":")).forEach(c -> {
				queue.add(VirtualKeyEvent.fromString(c));
			});
		} else {
			TASmod.endPlayback();
		}
	}
	
	private final void tickMouse() {
		final String line = linesRead.poll();
		if (line != null) {
			if (line.isEmpty()) return;
			final Queue<VirtualMouseEvent> queue = VirtualMouse.mouseEventsForTick;
			queue.clear();
			Arrays.asList(line.split(":")).forEach(c -> {
				VirtualMouseEvent c2 = VirtualMouseEvent.fromString(c);
				queue.add(c2);
				VirtualMouse.dX = c2.dX;
				VirtualMouse.dY = c2.dY;
			});
		}
	}
	
}

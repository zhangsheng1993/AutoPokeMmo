package auto.controller;

import java.awt.Rectangle;
import java.util.concurrent.ConcurrentHashMap;

public class AutoTaskManger {
	private ConcurrentHashMap<Class, AutoTask> taskMap;
	private static AutoTaskManger instance;
	private Rectangle targetRectangle = null;
	public static AutoTaskManger getInstance() {
		if (null == instance) {
			instance = new AutoTaskManger();
		}
		return instance;
	}

	private AutoTaskManger() {
		taskMap = new ConcurrentHashMap<Class, AutoTask>();
	};

	public void autoTaskUnRegister(Class class1) {
		taskMap.remove(class1);
	}

	public void autoTaskRegister(AutoTask autoTask, Class class1) {
		taskMap.put(class1, autoTask);

	}

	public void autoTaskExecut(byte[] bts) {
		for (AutoTask autoTask : taskMap.values()) {
			autoTask.executeTask(bts);
		}

	}

	public Rectangle getTargetRectangle() {
		return targetRectangle;
	}

	public void setTargetRectangle(Rectangle targetRectangle) {
		this.targetRectangle = targetRectangle;
	}
}

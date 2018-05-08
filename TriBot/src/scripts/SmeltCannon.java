package scripts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Game;
import org.tribot.api2007.GrandExchange;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.NPCChat;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Options;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;
import org.tribot.api2007.Interfaces;

@ScriptManifest(authors = { "Zesty" }, category = "Smithing", name = "SmeltCannon")
public class SmeltCannon extends Script implements Painting {

	private final int furnace = 16469; // furnace ID in Edgville
	private final int steelBar = 2353;
	private final int bank_booth = 6943;
	private final int BANK_MASTER_ID = 12, WITHDRAW_AS_ITEM_ID = 21, WITHDRAW_AS_NOTED_ID = 23;
	// this will count the amount of times we sell, after 10 transactions trade
	int counter = 0;
	Timer timer = new Timer(3600000);

	private boolean onStart() {
		setRandomSolverState(false);
		System.out.println("Script has started");
		return true;
	}

	@Override
	public void run() {
		if (onStart()) {
			while (true) {
				sleep(work());
			}
		}

	}

	private int work() {
		if (TimeCheck()) {

			RSObject[] furn = Objects.findNearest(10, furnace);
			if (furn.length > 0) {
				if (!furn[0].isOnScreen()) {
					println("came");
					RSTile tile = new RSTile(3106, 3498, 0);
					Walking.walkTo(tile);
					sleep(2000);
				}
			} else {
				returnToBase();
				return 45;
			}
			RSItem[] bar = Inventory.find(steelBar);
			sleep(1000);
			if (bar == null || bar.length <= 0) {
				sleep(2000);
				runToBank();
			} else {

				RSItem steel_Bar = bar[0];
				while (bar.length != 0) {

					if (!timer.isRunning()) {
						Login.logout();
						setLoginBotState(false);
						sleep(1200000);
						setLoginBotState(true);
						Login.login("4@rsgold.party", "partyrs007");
						timer.reset();
						return 45;
					}

					if (Player.getRSPlayer().getAnimation() == -1) {

						furn = Objects.findNearest(10, furnace);
						if (furn.length == 0) {
							returnToBase();
							sleep(3000);
							return 45;
						}
						RSObject burner = furn[0];
						bar = Inventory.find(steelBar);
						if (bar.length == 0) {
							println("out of steel bars, run to bank");
							runToBank();
							return 45;
						}
						steel_Bar.click("Use");
						Mouse.leaveGame();
						String uptext = Game.getUptext();
						if (uptext != null && uptext.equalsIgnoreCase("Use Steel Bar ->")) {
							burner.click("Use Steel Bar -> Furnace");
							sleep(3000);
							Mouse.clickBox(220, 390, 270, 440, 3);
							sleep(2000);
							Timing.waitChooseOption("Make all", 3000);
							sleep(15000);
							bar = Inventory.find(steelBar);
							if (bar.length == 0) {
								println("run to bank");
								runToBank();
								return 45;
							} else {
								println("steel bars left: " + bar.length);
							}
							steel_Bar = bar[bar.length - 1];
							bar = Inventory.find(steelBar);
							while (Player.getRSPlayer().getAnimation() != -1 && bar.length > 0) {
								sleep(20000);
								continue;
							}

						} else {
							sleep(15000);
						}
					}

				}

			}
		} else {
			Calendar c = new GregorianCalendar(TimeZone.getTimeZone("PST"));
			int time = c.get(Calendar.HOUR_OF_DAY);
			Login.logout();
			println("not in time limit");
			if (time >= 9 && time < 22) {
				setLoginBotState(true);
				sleep(7200000);
			}
			else
			{
				System.exit(0);
			}
		}

		return 45;

	}

	private boolean TimeCheck() {
		Calendar c = new GregorianCalendar(TimeZone.getTimeZone("PST"));
		int time = c.get(Calendar.HOUR_OF_DAY);
		if (time == 13 || time == 16 || time == 20)
			return false;
		else if (time >= 9 && time < 22)
			return true;
		else
			return false;

	}

	private void runToBank() {
		/*
		 * Camera.setCameraRotation(95); Camera.setCameraAngle(67); Point p =
		 * new Point(268, 35); Mouse.click(p, 1); sleep(5000);
		 * Camera.setCameraRotation(120); Camera.setCameraAngle(48);
		 * p.setLocation(275, 25); Mouse.click(p, 1); sleep(3000);
		 * BankInteract();
		 */
		sleep(1000);
		Options.setRunOn(false);
		WebWalking.walkToBank();
		sleep(5000);
		RSObject[] booth = Objects.findNearest(10, bank_booth);
		if (booth.length == 0) {
			runToBank();
			return;
		}
		BankInteract();
		return;

	}

	private void BankInteract() {
		RSObject[] booth = Objects.findNearest(10, bank_booth);
		booth[0].click("Bank");
		sleep(3000);
		RSItem[] barCheck = Banking.find("Steel bar");
		println(barCheck.length);
		sleep(3000);
		if (barCheck.length > 0) {
			Banking.withdraw(0, "Steel bar");
			sleep(3000);
			if (Inventory.find(2).length > 0)
				Banking.deposit(0, "Cannonball");
			else {
				/// Banking.deposit(1, "Steel bar");
				sleep(1000);
				Banking.close();
				sleep(3000);
				returnToBase();
				sleep(5000);
				return;
			}
			Banking.close();
			sleep(3000);
			returnToBase();
			return;
		} else {
			Options.setRunOn(false);
			runToGe();
			sleep(5000);
			returnToBase();
			return;
		}

	}

	/*
	 * run back to furnace
	 */

	private void returnToBase() {
		sleep(8000);
		Camera.setCameraRotation(294);
		Camera.setCameraAngle(46);
		Point p = new Point(231, 6);
		Mouse.click(p, 1);
		sleep(10000);
		RSTile tile = Player.getPosition();
		if (tile.getX() != 3106) {
			return;
		} else {
			p.setLocation(638, 74);
			Camera.setCameraRotation(323);
			Camera.setCameraAngle(61);
			Mouse.click(p, 1);
			return;
		}

	}

	private void runToGe() {

		RSTile[] pathToGe = { new RSTile(3101, 3499), new RSTile(3103, 3504), new RSTile(3119, 3507),
				new RSTile(3132, 3505), new RSTile(3132, 3490), new RSTile(3132, 3476), new RSTile(3139, 3466),
				new RSTile(3143, 3455), new RSTile(3156, 3463), new RSTile(3164, 3474), new RSTile(3165, 3485) };
		if (Game.getRunEnergy() > 90) {
			Options.setRunOn(true);
		} else {
			Options.setRunOn(false);
		}
		Walking.walkPath(pathToGe);
		sleep(20000);
		Camera.setCameraRotation(334);
		Camera.setCameraAngle(77);
		geInteract();
		return;

	}

	private void geInteract() {
		sleep(3000);
		RSNPC[] banker_List = NPCs.findNearest(1000, 5453);
		RSNPC banker = banker_List[0];
		RSNPC[] geClerk = NPCs.findNearest(1000, 2151);
		RSNPC clerk = geClerk[0];
		banker.click("Talk-to");
		sleep(3000);
		NPCChat.clickContinue(true);
		sleep(3000);
		NPCChat.selectOption("I'd like to access my bank account, please.", true);
		NPCChat.clickContinue(active);
		sleep(5000);
		// RSItem[] cannonBalls = Banking.find("Cannonball");
		Banking.withdraw(0, "Cannonball");
		Banking.withdraw(0, "Coins");
		boolean sellOld = false;
		if (Banking.find("Amulet of glory").length > 0) {
			if (Banking.find("Amulet of glory")[0].getStack() >= 10) {
				sellOld = true;
				setNoted(true);
				Banking.withdraw(0, "Amulet of glory");
			}
		}
		sleep(3000);
		Banking.close();
		sleep(2000);
		clerk.click("Exchange");
		sleep(3000);
		NPCChat.clickContinue(true);
		sleep(3000);
		NPCChat.selectOption("I'd like to set up trade offers please.", true);
		sleep(5000);
		NPCChat.clickContinue(false);
		sleep(7000);
		RSItem[] cannonCheck = Inventory.find("Cannonball");
		if (cannonCheck.length != 0) {
			sellAtMinus5("Cannonball");
			sleep(2000);
			Interfaces.get(465, 6).getChild(1).click("Collect");

		}
		if (sellOld) {
			sellAtMinus5("Amulet of glory");
			sleep(3000);
			Interfaces.get(465, 6).getChild(1).click("Collect");
		}

		// GrandExchange.offer("Cannonball", -1, inStock, true);
		// Could be null - Nothing I can do atm tho takes a lot of code to fix,
		// will do in future
		// General.sleep(4000);
		// GrandExchange.confirmOffer();
		// sleep(30000);
		// collected money, buy steel bars...
		buyAtPlus5("Steel Bar", 1000);
		sleep(60000);
		Interfaces.get(465, 6).getChild(1).click("Collect");
		GrandExchange.close();
		sleep(7000);
		// deposit coins and bars...
		banker.click("Talk-to");
		sleep(5000);
		NPCChat.clickContinue(true);
		sleep(1000);
		NPCChat.selectOption("I'd like to access my bank account, please.", true);
		sleep(4000);
		NPCChat.clickContinue(active);
		sleep(4000);

		Banking.deposit(0, "Steel bar");
		Banking.deposit(0, "Coins");

		// withdraw amulet of glory
		String[] GloryIDs = { "Amulet of glory(1)", "Amulet of glory(2)", "Amulet of glory(3)", "Amulet of glory(4)",
				"Amulet of glory(5)", "Amulet of glory(6)" };

		RSItem[] op1 = Banking.find(GloryIDs[0]);
		RSItem[] op2 = Banking.find(GloryIDs[1]);
		RSItem[] op3 = Banking.find(GloryIDs[2]);
		RSItem[] op4 = Banking.find(GloryIDs[3]);
		RSItem[] op5 = Banking.find(GloryIDs[4]);
		RSItem[] op6 = Banking.find(GloryIDs[5]);

		if (op1.length == 0 && op2.length == 0 && op3.length == 0 && op4.length == 0 && op5.length == 0
				&& op6.length == 0) {
			sleep(3000);
			Banking.withdraw(0, "Coins");
			Banking.close();
			sleep(5000);
			clerk.click("Exchange");
			sleep(5000);
			NPCChat.clickContinue(true);
			sleep(5000);
			NPCChat.selectOption("I'd like to set up trade offers please.", true);
			sleep(5000);
			NPCChat.clickContinue(true);
			sleep(5000);
			buyAtPlus5("Amulet of glory(6)", 1);
			sleep(30000);
			Interfaces.get(465, 6).getChild(1).click("Collect");
			GrandExchange.close();
			gloryTeleport();
			return;
		}

		if (op1.length > 0) {
			Banking.withdraw(1, "Amulet of glory(1)");
			sleep(3000);
		} else if (op2.length > 0) {
			Banking.withdraw(1, "Amulet of glory(2)");
			sleep(3000);
		} else if (op3.length > 0) {
			Banking.withdraw(1, "Amulet of glory(3)");
			sleep(3000);
		} else if (op4.length > 0) {
			Banking.withdraw(1, "Amulet of glory(4)");
			sleep(3000);
		} else if (op5.length > 0) {
			Banking.withdraw(1, "Amulet of glory(5)");
			sleep(3000);
		} else if (op6.length > 0) {
			Banking.withdraw(1, "Amulet of glory(6)");
			sleep(3000);
		}

		Banking.close();
		// sleep(5000);
		gloryTeleport();
		return;

	}

	private void gloryTeleport() {
		sleep(3000);
		String[] GloryIDs = { "Amulet of glory(1)", "Amulet of glory(2)", "Amulet of glory(3)", "Amulet of glory(4)",
				"Amulet of glory(5)", "Amulet of glory(6)" };
		RSItem[] amulet = Inventory.find(GloryIDs);
		amulet[0].click("Wear");
		sleep(4000);
		RSItem[] glories = Equipment.find(GloryIDs);
		RSItem glory = glories[0];
		sleep(2000);
		glory.click("Edgeville");
		sleep(4000);

		// checks if glory out of charges

		boolean outOfGlory = false;
		if (Equipment.find("Amulet of glory").length != 0) {
			Equipment.remove("Amulet of glory");
			outOfGlory = true;
		} else {
			Equipment.remove(GloryIDs);
		}
		sleep(3000);
		Inventory.open();
		Camera.setCameraRotation(271);
		Camera.setCameraAngle(62);
		Point p = new Point(645, 55);
		Mouse.click(p, 1);
		sleep(7000);
		RSObject[] booth = Objects.findNearest(10, bank_booth);
		booth[0].click("Bank");
		sleep(5000);
		Banking.deposit(0, "Coins");
		if (outOfGlory) {
			Banking.deposit(0, "Amulet of glory");
		} else {
			Banking.deposit(0, GloryIDs);
		}
		sleep(2000);
		Banking.withdraw(0, "Steel bar");
		Banking.close();
		sleep(3000);
		// returnToBase();
		return;

	}

	private static boolean buyAtPlus5(String item, int amount) {
		if (amount < 1)
			return true;
		for (int i = 7; i < 15; i++) {
			if (Interfaces.get(465, i) != null) {
				if (!Interfaces.get(465, i).getChild(3).isHidden()) {
					if (Interfaces.get(465, i).getChild(3).click("Create Buy offer")) {
						General.sleep(300, 800);
						if (GrandExchange.setItem(item)) {
							General.sleep(300, 800);
							if (GrandExchange.setQuantity(amount)) {
								General.sleep(300, 800);
								if (Interfaces.get(465, 24).getChild(53).click("+5%")) {
									General.sleep(300, 800);
									if (GrandExchange.confirmOffer()) {
										General.sleep(300, 800);
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private static boolean sellAtMinus5(String item) {
		final RSItem[] items = Inventory.find(item);
		if (items.length > 0) {
			if (items[0].click("Offer")) {
				General.sleep(300, 800);
				if (Interfaces.get(465, 24).getChild(50).click("-5%")) {
					General.sleep(200, 700);
					if (GrandExchange.confirmOffer()) {
						General.sleep(300, 800);
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isNotedOn() {
		if (Banking.isBankScreenOpen()) {
			RSInterfaceChild itemInterfaceChild = Interfaces.get(BANK_MASTER_ID, WITHDRAW_AS_ITEM_ID);
			if (itemInterfaceChild != null) {
				return itemInterfaceChild.getTextureID() == 812;
			}
		}
		return false;
	}

	public boolean setNoted(boolean noted) {
		if (Banking.isBankScreenOpen()) {
			if (noted && !isNotedOn()) {
				RSInterfaceChild notedInterfaceChild = Interfaces.get(BANK_MASTER_ID, WITHDRAW_AS_NOTED_ID);
				if (notedInterfaceChild != null && notedInterfaceChild.click("Note")) {
					Timing.waitCondition(new Condition() {
						@Override
						public boolean active() {
							General.sleep(10, 30);
							return isNotedOn();
						}
					}, General.random(800, 1200));
					return isNotedOn();
				}
			} else if (!noted && isNotedOn()) {
				RSInterfaceChild itemInterfaceChild = Interfaces.get(BANK_MASTER_ID, WITHDRAW_AS_ITEM_ID);
				if (itemInterfaceChild != null && itemInterfaceChild.click("Item")) {
					Timing.waitCondition(new Condition() {
						@Override
						public boolean active() {
							General.sleep(10, 30);
							return !isNotedOn();
						}
					}, General.random(800, 1200));
					return !isNotedOn();
				}
			}
		}
		return false;
	}

	@Override
	public void onPaint(Graphics g) {

		g.setColor(Color.RED);
		g.drawString("Script: SmeltCannon", 380, 330);
	}

}

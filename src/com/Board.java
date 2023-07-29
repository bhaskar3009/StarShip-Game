package com;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.Timer;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import sprite.Alien;
import sprite.Player;
import sprite.Shot;

public class Board extends JPanel{

	private Dimension d;
	private List<Alien> aliens;
	private Player player;
	private Shot shot;
	
	private int dir = -1;
	private int deaths = 0;
	
	private boolean inGame = true;
	private String explsnImg = "src/images/explosion.png";
	private String msg = "Game Over";
	
	private Timer timer;
	
	public Board() {
		initBoard();
		initGame();	
	}
	
	private void initBoard() {
		
		addKeyListener(new TAdapter());
		
		setFocusable(true);
		d = new Dimension(Params.BOARD_WIDTH, Params.BOARD_HEIGHT);
		setBackground(Color.black);
		timer = new Timer(Params.DELAY, new GameCycle());
		timer.start();
		
		initGame();
	}
	private void initGame() {
		aliens =new ArrayList<>();
		
		for(int i=0; i<4; i++) {
			for(int j=0; j<6; j++) {
				var alien =new Alien(Params.ALIEAN_INIT_X + 18*j, Params.ALIEAN_INIT_Y + 18*i);
				aliens.add(alien);
			}
		}
		
		player =new Player();
		shot =new Shot();
		
	}
	
	private void drawAliens(Graphics g) {
		for(Alien alien: aliens) {
			if(alien.isVisible()) {
				g.drawImage(alien.getImage(), alien.getX(), alien.getY(), this);
			}
			if(alien.isDying()) {
				alien.die();
			}
		}
	}
	
	private void drawPlayer(Graphics g) {
		if(player.isVisible()) {
			g.drawImage(player.getImage(), player.getX(), player.getY(), this);
		}
		if(player.isDying()) {
			player.die();
			inGame = false;
		}
	}
	
	private void drawShot(Graphics g) {
		if(shot.isVisible()) {
			g.drawImage(shot.getImage(), shot.getX(), shot.getY(), this);

		}
	}
	
	private void drawBomb(Graphics g) {
		for(Alien a:aliens) {
			Alien.Bomb b = a.getBomb();
			
			if(!b.isDestroyed()) {
				g.drawImage(b.getImage(), b.getX(), b.getY(), this);
			}
		}
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		drawing(g);
	}	
	
	private void drawing(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, d.width, d.height);
		g.setColor(Color.green);
		
		if(inGame) {
			g.drawLine(0, Params.BOARD_GROUND, Params.BOARD_WIDTH, Params.BOARD_GROUND);
			
			drawAliens(g);
			drawPlayer(g);
			drawShot(g);
			drawBomb(g);
		}
		else {
			if(timer.isRunning()) {
				timer.stop();
			}
			gameOver(g);
		}
		Toolkit.getDefaultToolkit().sync();
	}
	
	private void gameOver(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, Params.BOARD_WIDTH, Params.BOARD_HEIGHT);
		
		g.setColor(new Color(0, 32, 48));
		g.fillRect(50, Params.BOARD_WIDTH/2 - 30, Params.BOARD_WIDTH - 100, 50);
	
		g.setColor(Color.white);
		g.drawRect(50, Params.BOARD_WIDTH/2 - 30, Params.BOARD_WIDTH - 100, 50);
	
		var small =new Font("Helvetica", Font.BOLD, 14);
		var fontMatrics = this.getFontMetrics(small);
		
		g.setColor(Color.white);
		g.setFont(small);
		g.drawString(msg, (Params.BOARD_WIDTH - fontMatrics.stringWidth(msg))/2, Params.BOARD_WIDTH/2);
		
	
	}
	
	private void update() {
		
		if(deaths == Params.NO_ALIENS) {
			inGame = false;
			timer.stop();
			
			msg = "You Won!";
		}
		
		player.act();
		
		if(shot.isVisible()) {
			int shotX = shot.getX();
			int shotY = shot.getY();
			
			for(Alien alien:aliens) {
				int alienX = alien.getX();
				int alienY = alien.getY();
				
				if(alien.isVisible() && shot.isVisible()) {
					if(shotX >= (alienX) && shotX <= (alienX + Params.ALIEAN_WIDTH ) && shotY >= (alienY) && shotY <= (alienY + Params.ALIEN_HEIGHT)) {
						var img =new ImageIcon(explsnImg);
						alien.setImage(img.getImage());
						
						alien.setDying(true);
						deaths++;
						shot.die();
					}
				}
			}
			int y = shot.getY();
			y-=4;
			
			if(y<0) {
				shot.die();
			}else {
				shot.setY(y);
			}
		}
		
		for(Alien alien : aliens) {
			int x =alien.getX();
			
			if(x>=Params.BOARD_WIDTH - Params.BORDER_RIGHT && dir !=-1) {
				dir =-1;
				
				Iterator<Alien> itr1 = aliens.iterator();
				
				while(itr1.hasNext()) {
					Alien a2 = itr1.next();
					a2.setY(a2.getY()+Params.GO_DOWN);
				}
			}
			
			if(x<=Params.BORDER_LEFT && dir !=1) {
				dir = 1;
				Iterator<Alien> itr2 = aliens.iterator();
				
				while(itr2.hasNext()) {
					Alien a = itr2.next();
					a.setY(a.getY()+Params.GO_DOWN);
				}
				
			}
			
		}
		Iterator<Alien> itr = aliens.iterator();
		while(itr.hasNext()) {
			Alien alien =itr.next();
			if(alien.isVisible()) {
				int y=alien.getY();
				if(y> Params.BOARD_GROUND - Params.ALIEN_HEIGHT) {
					inGame = false;
					msg = "Invasion!";
					
				}
				alien.act(dir);
			}
		}
		 var gnrtr = new Random();
		 for(Alien alien: aliens) {
			 int shot = gnrtr.nextInt(15);
			 Alien.Bomb bomb=alien.getBomb();
			 
			 if(shot == Params.CHANCE && alien.isVisible() && bomb.isDestroyed()) {
				 bomb.setDestroyed(false);
				 bomb.setX(alien.getX());
				 bomb.setY(alien.getY());
			 }
			 int bombX = bomb.getX();
			 int bombY = bomb.getY();
			 int playerX = player.getX();
			 int playerY = player.getY();
			 
			 if(player.isVisible() && !bomb.isDestroyed()) {
				 if(bombX >=(playerX) && bombX <=(playerX + Params.PLAYER_WIDTH) && bombY >=(playerY) && bombY <=(playerY + Params.PLAYER_HEIGHT) ) {
					 
					 var img = new ImageIcon(explsnImg);
					 player.setImage(img.getImage());
					 player.setDying(true);
					 bomb.setDestroyed(true);
				 }
			 }
			 
			 if(!bomb.isDestroyed()) {
				 bomb.setY(bomb.getY() + 1);
				 
				 if(bomb.getY() >= Params.BOARD_GROUND - Params.BOMB_HEIGHT ) {
					 bomb.setDestroyed(true);
				 }
			 }
	     }

	}
	
	private void doGameCycle() {
		update();
		repaint();
		
	}
	
	private class GameCycle implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			doGameCycle();
		}
	}
	
	private class TAdapter extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent e) {
			player.KeyReleased(e);
		}
		
		@Override
		public void keyPressed(KeyEvent e) {
			player.keyPressed(e);
			
			int x = player.getX();
			int y = player.getY();
			
			int key = e.getKeyCode();
			
			if(key == KeyEvent.VK_SPACE) {
				if(inGame) {
					if(!shot.isVisible()) {
						shot = new Shot(x, y);
					}
				}
			}
		}
	}
	
}

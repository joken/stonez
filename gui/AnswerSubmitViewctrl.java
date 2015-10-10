package com.procon.gui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AnswerSubmitViewctrl {
	@FXML private Label LogArea;
	@FXML private TextField HostArea;
	@FXML private TextArea AnswerArea;
	@FXML private Button Submitbtn;

	@FXML
	private void onSubmit(){
		this.Submit(HostArea.getText(), AnswerArea.getText());
	}

	/**
	 * 解答提出システムのクライアント(の改造)．
	 *
	 * $ java SubmitClient host
	 *
	 * @author Mandai
	 *
	 */
	private void Submit(String hostaddr,String answer) {
		Task<Void> t = new Task<Void>(){
			@Override
			protected Void call() throws Exception{
				updateMessage("Connecting to " + hostaddr + "...\n");
				try (
				// 標準入力を準備
				Scanner stdIn = new Scanner(answer);
						// リモートに接続
						Socket sock = new Socket(hostaddr, 65432);) {
					updateMessage("Connected.\n");

					// リモートからの入力を準備
					BufferedReader in = new BufferedReader(new InputStreamReader(
							sock.getInputStream()));
					// リモートへの出力を準備
					PrintWriter out = new PrintWriter(sock.getOutputStream(), true);

					// 標準入力から解答を読む
					// <想定フォーマット> := <スコア> <石数> <解答の行数>
					int score = stdIn.nextInt();
					int num_stones = stdIn.nextInt();
					int num_lines = stdIn.nextInt();
					// 改行を読み飛ばす
					stdIn.nextLine();
					// 解答を読み込む
					String data = "";
					for (int i = 0; i < num_lines; i++) {
						data += stdIn.nextLine() + "\n";
					}

					// リモートに送信
					out.printf("%d %d %d\n", score, num_stones, num_lines);
					out.println(data);

					// リモートからの応答を受信
					String result = in.readLine();
					updateMessage("Reply from " + hostaddr + ": " + result + "\n");

				} catch (Exception e) {
					updateMessage(e.getMessage());
				}
				return null;
			}
		};
		new Thread(t).start();
		while(t.isRunning()){
			LogArea.setText(t.getMessage());
		}
	}
}

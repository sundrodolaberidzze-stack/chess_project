package org.example.chess.engine;

import org.example.chess.models.Board;
import org.example.chess.models.Move;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class StockfishAI {
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final Random random = new Random();

    public boolean startEngine() {
        try {
            Path path = findEnginePath();
            if (path == null) return false;
            process = new ProcessBuilder(path.toString()).redirectErrorStream(true).start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            sendCommand("uci");
            waitFor("uciok", 5000);
            sendCommand("isready");
            waitFor("readyok", 5000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Path findEnginePath() throws IOException {
        String[] paths = {
                "src/main/resources/Stockfish/stockfish/stockfish-windows-x86-64-avx2.exe",
                "src/main/resources/Stockfish/stockfish-windows-x86-64-avx2/stockfish/stockfish-windows-x86-64-avx2.exe",
                "src/main/resources/Stockfish/stockfish-windows-x86-64-avx2.exe"
        };

        for (String p : paths) {
            Path path = Path.of(p);
            if (Files.exists(path)) return path.toAbsolutePath();
        }

        Path stockfishFolder = Path.of("src/main/resources/Stockfish");
        if (Files.exists(stockfishFolder)) {
            try (Stream<Path> stream = Files.walk(stockfishFolder)) {
                Path found = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".exe"))
                        .filter(p -> p.getFileName().toString().toLowerCase().contains("stockfish"))
                        .findFirst()
                        .orElse(null);
                if (found != null) return found.toAbsolutePath();
            }
        }

        String[] resources = {
                "/Stockfish/stockfish/stockfish-windows-x86-64-avx2.exe",
                "/Stockfish/stockfish-windows-x86-64-avx2/stockfish/stockfish-windows-x86-64-avx2.exe",
                "/Stockfish/stockfish-windows-x86-64-avx2.exe"
        };

        for (String resource : resources) {
            InputStream in = getClass().getResourceAsStream(resource);
            if (in != null) {
                Path temp = Files.createTempFile("stockfish", ".exe");
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
                temp.toFile().setExecutable(true);
                temp.toFile().deleteOnExit();
                return temp;
            }
        }

        return null;
    }

    private void waitFor(String wanted, long timeoutMs) throws IOException {
        long end = System.currentTimeMillis() + timeoutMs;
        String line;
        while (System.currentTimeMillis() < end && (line = reader.readLine()) != null) {
            if (line.contains(wanted)) return;
        }
    }

    private void sendCommand(String cmd) throws IOException {
        writer.write(cmd);
        writer.newLine();
        writer.flush();
    }

    public String getBestMove(String fen, int difficulty) {
        try {
            if (process == null || !process.isAlive()) return null;

            int level = Math.max(0, Math.min(20, difficulty));
            configureDifficulty(level);

            sendCommand("position fen " + fen);
            sendCommand("go movetime " + getMoveTime(level));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    String[] parts = line.split(" ");
                    if (parts.length > 1 && !parts[1].equals("(none)")) return parts[1];
                    return null;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private void configureDifficulty(int level) throws IOException {
        if (level >= 20) {
            sendCommand("setoption name UCI_LimitStrength value false");
            sendCommand("setoption name Skill Level value 20");
        } else {
            int elo = 1320 + (level * 75);
            sendCommand("setoption name UCI_LimitStrength value true");
            sendCommand("setoption name UCI_Elo value " + elo);
            sendCommand("setoption name Skill Level value " + level);
        }
        sendCommand("isready");
        waitFor("readyok", 5000);
    }

    private int getMoveTime(int level) {
        if (level >= 20) return 3000;
        return 500 + level * 100;
    }

    public Move fallbackMove(Board board) {
        List<Move> moves = board.getAllLegalMoves(board.getTurn());
        if (moves.isEmpty()) return null;
        return moves.get(random.nextInt(moves.size()));
    }

    public void stopEngine() {
        try {
            if (writer != null) sendCommand("quit");
        } catch (Exception ignored) {}
        if (process != null) process.destroy();
    }
}

public class CallLogLifecycle {

    // Define possible states
    public enum State {
        INIT,
        THREAD_SWITCH,
        FILE_ROTATION,
        DIFF_DETECTION,
        UPLOAD,
        SYNC_FILES,
        NO_CHANGE,
        UI_UPDATE,
        ERROR,
        COMPLETE
    }

    private State currentState = State.INIT;

    public void performFetch() {
        try {
            transition(State.THREAD_SWITCH);
            runOnIOThread(() -> {
                transition(State.FILE_ROTATION);
                manager.saveCallLogs();

                transition(State.DIFF_DETECTION);
                DiffResult diff = compareFiles("oldFile", "newFile");

                if (!diff.newEntries.isEmpty()) {
                    if (config.hasWebhookUrl()) {
                        transition(State.UPLOAD);
                        uploadToDiscord(diff);

                        transition(State.SYNC_FILES);
                        syncBothIfDifferent();
                    }
                } else {
                    transition(State.NO_CHANGE);
                    // Skip upload and sync
                }

                transition(State.UI_UPDATE);
                runOnMainThread(() -> {
                    String message = "Sync Complete! New Entries: " + diff.newEntries.size();
                    updateTextView(message);
                });

                transition(State.COMPLETE);
            });
        } catch (Exception e) {
            transition(State.ERROR);
            runOnMainThread(() -> updateTextView("Error: " + e.getMessage()));
            transition(State.COMPLETE);
        }
    }

    private void transition(State newState) {
        System.out.println("Transitioning from " + currentState + " to " + newState);
        currentState = newState;
    }

    // --- Mocked helper methods ---
    private void runOnIOThread(Runnable task) {
        new Thread(task).start();
    }

    private void runOnMainThread(Runnable task) {
        // In Android, you'd use Handler/Looper or LiveData
        task.run();
    }

    private DiffResult compareFiles(String oldFile, String newFile) {
        // Mock comparison logic
        return new DiffResult();
    }

    private void uploadToDiscord(DiffResult diff) {
        // Mock upload logic
    }

    private void syncBothIfDifferent() {
        // Mock sync logic
    }

    private void updateTextView(String message) {
        System.out.println(message);
    }

    // --- Supporting class ---
    static class DiffResult {
        java.util.List<String> newEntries = new java.util.ArrayList<>();
    }
}

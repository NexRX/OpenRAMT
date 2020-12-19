package Controller;

public class LoginService extends ProgressibleService<String, String> {

    @Override
    protected String backgroundTask(String param) {
        String exitStatus = "Healthy";
        this.setProgress(0f);

        for (int i = 0; i < 10; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.addProgress(0.1F);
        }
        return exitStatus;
    }

    @Override
    protected void finalTask(String params) {
        //Handles the return of the background. Useful for error logging.
    }
}

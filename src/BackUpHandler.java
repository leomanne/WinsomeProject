public class BackUpHandler implements Runnable{
    private long bkupTimeOut;//timer used for sleep
    public BackUpHandler(long bkupTimeOut) {
        this.bkupTimeOut = bkupTimeOut;
    }


    @Override
    public void run() {
        DTStructure.createFile();
        while (!Thread.currentThread().isInterrupted()) {//until thread is interrupted, sleep and get BackUp
            try {
                Thread.sleep(bkupTimeOut);//sleep
            } catch (InterruptedException e) {
                return;
            }
            if (!Thread.currentThread().isInterrupted()) {//execute the writing of current data
                DTStructure.BackUp();
            }
        }
        DTStructure.BackUp();

    }
}

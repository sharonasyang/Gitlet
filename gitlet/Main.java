package gitlet;

import java.io.File;
import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Sharona Yang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        final String gitfiles = ".gitlet/gitFiles";
        File gitfile = new File(gitfiles);
        if (!gitfile.exists()) {
            if (args[0].equals(("init"))) {
                Obj ran = new Obj();
                ran.init();
                Utils.writeObject(gitfile, ran);
            } else {
                System.out.println("Not in an initialized Gitlet directory.");
            }
        } else {
            mainFunc(gitfile, args);
        }
        System.exit(0);
    }

    public static void mainFunc(File gitfile, String... args) {
        Obj ran = Utils.readObject(gitfile, Obj.class);
        if (args[0].equals(("init"))) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        } else if (args[0].equals("add")) {
            ran.add(args[1]);
        } else if (args[0].equals("commit")) {
            ran.commit(args[1]);
        } else if (args[0].equals("log")) {
            ran.log();
        } else if (args[0].equals("checkout")) {
            if (args.length == 2) {
                ran.checkoutBranch(args[1]);
            } else if (args.length == 3) {
                ran.checkoutFile(args[2]);
            } else if (args.length == 4) {
                if (args[2].equals("++")) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                ran.checkoutCommit(args[3], args[1]);
            } else {
                System.out.println("Incorrect operands.");
            }
        } else if (args[0].equals("rm")) {
            ran.rm(args[1]);
        } else if (args[0].equals("global-log")) {
            ran.globalLog();
        } else if (args[0].equals("find")) {
            ran.find(args[1]);
        } else if (args[0].equals("branch")) {
            ran.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            ran.rmBranch(args[1]);
        } else if (args[0].equals("status")) {
            ran.status();
        } else if (args[0].equals("reset")) {
            ran.reset(args[1]);
        } else if (args[0].equals("merge")) {
            ran.merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
        }
        Utils.writeObject(gitfile, ran);
    }
}

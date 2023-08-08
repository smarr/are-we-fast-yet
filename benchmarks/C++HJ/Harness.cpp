#include "Run.cpp"
#include <string>
#include <vector>

using namespace std;

class Harness {
    public:

        static Run processArguments(char** args, int argc) {
            Run run = Run(args[1]);
            if (argc > 2) {
                run.setNumIterations(atoi(args[2]));
                if (argc > 3) {
                    run.setInnerIterations(atoi(args[3]));
                }
            }
            return run;
        }

        static void printUsage() {
            cout << "Harness [benchmark] [num-iterations [inner-iter]]" << endl << endl;
            cout << "  benchmark      - benchmark class name " << endl;
            cout << "  num-iterations - number of times to execute benchmark, default: 1" << endl;
            cout << "  inner-iter     - number of times the benchmark is executed in an inner loop, " << endl;
            cout << "                   which is measured in total, default: 1" << endl;
        }


        ~Harness() {
        }
};

int main(int argc, char** argv) {
    Harness harness = Harness();
    if (argc < 2) {
        harness.printUsage();
        return 1;
    }

    Run run = harness.processArguments(argv, argc);
    run.runBenchmark();
    run.printTotal();
}

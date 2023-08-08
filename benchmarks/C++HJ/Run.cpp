#include "Bounce.cpp"
#include "Mandelbrot.cpp"
#include "Sieve.cpp"
#include "List.cpp"
#include "Permute.cpp"
#include "Queens.cpp"
#include "Towers.cpp"
#include <string>
#include <functional>
#include <memory>
#include <vector>
#include <iostream>
#include <chrono>
#include "Benchmark.cpp"
#include "NBody.cpp"
#include "Json.cpp"
#include "Havlak.cpp"
#include "DeltaBlue.cpp"
#include "Richards.cpp"
#include "CD.cpp"

using namespace std;

class Run{
    private: 
        string _name;
        shared_ptr<Benchmark> _benchmarkSuite;
        int _numIterations;
        int _innerIterations;
        long _total;

        static shared_ptr<Benchmark> getSuiteFromName(const string &name) {

            if (name == "Bounce")
                return make_shared<Bounce>();
            if (name == "Sieve")
                return make_shared<Sieve>();
            if (name == "List")
                return make_shared<List>();
            if (name == "Mandelbrot")
                return make_shared<Mandelbrot>();
            if (name == "Permute")
                return make_shared<Permute>();
            if (name == "Queens")
                return make_shared<Queens>();
            if (name == "Towers")
                return make_shared<Towers>();
            if (name == "NBody")
                return make_shared<nbody::NBody>();
            if (name == "Json")
                return make_shared<json::Json>();
            if (name == "Havlak")
                return make_shared<havlak::Havlak>();
            if (name == "DeltaBlue")
                return make_shared<deltablue::DeltaBlue>();
            if (name == "Richards")
                return make_shared<richards::Richards>();
            if (name == "CD")
                return make_shared<CD::CD>();
            
            throw Error("No benchmark found with the name: " + name);
        }


        void measure(shared_ptr<Benchmark> bench) {
            auto startTime = chrono::high_resolution_clock::now();

            if(!bench->innerBenchmarkLoop(_innerIterations)) {
                throw Error("Benchmark fail with incorrect result");
            }
            auto endTime = chrono::high_resolution_clock::now();
            long runTime = chrono::duration_cast<chrono::nanoseconds>(endTime - startTime).count() / 1000;

            printResult(runTime);
            _total += runTime;
        };

        void doRuns(shared_ptr<Benchmark> bench) {
            for (int i = 0; i < _numIterations; i++)
                measure(bench);
        };

        void reportBenchmark() {
            cout << _name << ": iterations=" << _numIterations << " average: " 
            << (_total / _numIterations) << "us total: " << _total << "us" << endl;
        }

        void printResult(long runTime) {
            cout << _name << ": iterations=1 runtime: " << runTime << "us" << endl;
        }
    
    public: 

        Run(const string& name) {
            _name = name;
            _benchmarkSuite = getSuiteFromName(name);
            _numIterations = 1;
            _innerIterations = 1;
            _total = 0;
        }

        void runBenchmark() {
            cout << "Starting " << _name << " benchmark ..." << endl;

            doRuns(_benchmarkSuite);
            reportBenchmark();

            cout << endl;
        }

        void printTotal() {
            cout << "Total Runtime: " << _total << "us" << endl;
        }

        void setNumIterations(int numIterations) {
            _numIterations = numIterations;
        }  

        void setInnerIterations(int innerIterations) {
            _innerIterations = innerIterations;
        }
};
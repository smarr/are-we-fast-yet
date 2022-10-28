## Usage

The JavaScript benchmarks can be run directly with a recent Node.js,
for instance with:

```
node harness.js Queens 10 10
```

For other JavaScript runtime systems, the benchmarks may need to be 
preprocessed. This repo contains the setup to use Bable and Webpack to compile
the scripts into a more appropiate JavaScript version, or at least a single
file.

To create a single ECMAScript 5 compatible file, run:

```
npm install .
npm run webpack
```

This will create a `dist/harness.es5.js` and a `dist/harness.es2022.js` file.
These can be executed as before:

```
node dist/harness.es5.js Queens 10 10
node dist/harness.es2022.js Queens 10 10
```

The `webpack.config.js` file shows these two configurations,
and can be adapted to include other targets.
